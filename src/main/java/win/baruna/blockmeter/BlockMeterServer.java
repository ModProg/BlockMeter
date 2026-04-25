package win.baruna.blockmeter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import win.baruna.blockmeter.measurebox.MeasureBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockMeterServer implements ModInitializer {

    private final Map<UUID, List<MeasureBox>> playerBoxes = new HashMap<>();
    private MinecraftServer server;
    private static BlockMeterServer instance;

    @Override
    public void onInitialize() {

        instance = this;
        PayloadTypeRegistry.clientboundPlay().register(BoxPayload.ID, BoxPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BoxPayload.ID, BoxPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BoxPayload.ID, this::processClientPacket);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);
        ServerPlayConnectionEvents.DISCONNECT.register((b, a) -> {
            System.out.printf("DISCONNECTD %s%n", b.player.getUUID());
        });
    }

    /**
     * Removes a player from the BoxMap
     *
     * @param player Player to be removed
     */
    public static void removePlayer(ServerPlayer player) {
        if (instance != null) {
            if (instance.playerBoxes.containsKey(player.getUUID())) {
                instance.playerBoxes.remove(player.getUUID());
                instance.informAllPlayers();
            }
        }
    }

    /**
     * Handles ServerStart
     */
    private void onStartServer(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Processes a ClientPacket
     */

    private void processClientPacket(BoxPayload payload, ServerPlayNetworking.Context context) {
        try {
            var boxes = payload.receivedBoxes().values().stream().findFirst().orElse(List.of());
            synchronized (playerBoxes) {
                playerBoxes.put(context.player().getUUID(), boxes);
            }
        } catch (IllegalArgumentException ex) {
            synchronized (playerBoxes) {
                playerBoxes.remove(context.player().getUUID());
            }
        }

        server.execute(this::informAllPlayers);
    }

    /**
     * Informs all Players about the current boxList
     */
    private void informAllPlayers() {
        BoxPayload data = buildS2CPacket();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, data);
        }
    }

    /**
     * Builds a S2CPacket containing the BoxList
     *
     * @return S2CPacket containing the BoxList
     */
    private BoxPayload buildS2CPacket() {
        var data = new HashMap<String, List<MeasureBox>>();
        synchronized (playerBoxes) {
            for (var playerBoxEntry : playerBoxes.entrySet()) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerBoxEntry.getKey());
                if (player != null) {
                    data.put(player.getScoreboardName(), playerBoxEntry.getValue());
                }
            }
        }
        return new BoxPayload(data);
    }
}
