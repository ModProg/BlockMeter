package win.baruna.blockmeter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
        PayloadTypeRegistry.playS2C().register(BoxPayload.ID, BoxPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BoxPayload.ID, BoxPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BoxPayload.ID, this::processClientPacket);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);
    }

    /**
     * Removes a player from the BoxMap
     *
     * @param player Player to be removed
     */
    public static void removePlayer(ServerPlayerEntity player) {
        if (instance != null) {
            if (instance.playerBoxes.containsKey(player.getUuid())) {
                instance.playerBoxes.remove(player.getUuid());
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
                playerBoxes.put(context.player().getUuid(), boxes);
            }
        } catch (IllegalArgumentException ex) {
            synchronized (playerBoxes) {
                playerBoxes.remove(context.player().getUuid());
            }
        }

        server.execute(this::informAllPlayers);
    }

    /**
     * Informs all Players about the current boxList
     */
    private void informAllPlayers() {
        BoxPayload data = buildS2CPacket();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
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
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerBoxEntry.getKey());
                if (player != null) {
                    data.put(player.getNameForScoreboard(), playerBoxEntry.getValue());
                }
            }
        }
        return new BoxPayload(data);
    }
}
