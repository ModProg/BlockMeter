package win.baruna.blockmeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import win.baruna.blockmeter.measurebox.MeasureBox;

public class BlockMeterServer implements ModInitializer {

    private Map<UUID, List<MeasureBox>> playerBoxes;
    private MinecraftServer server;
    private static BlockMeterServer instance;

    @Override
    public void onInitialize() {

        instance = this;
        playerBoxes = new HashMap<>();
        ServerPlayNetworking.registerGlobalReceiver(BlockMeter.C2SPacketIdentifier, this::processClientPacket);
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
     * 
     * @param server
     */
    private void onStartServer(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Processes a ClientPacket
     * 
     * @param packetContext
     * @param attachedData
     */

    private void processClientPacket(MinecraftServer server, ServerPlayerEntity player,
            ServerPlayNetworkHandler handler, PacketByteBuf attachedData, PacketSender responseSender) {
        int size = attachedData.readInt();

        try {
            List<MeasureBox> clientBoxes = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                clientBoxes.add(MeasureBox.fromPacketByteBuf(attachedData));
            }
            synchronized (playerBoxes) {
                playerBoxes.put(player.getUuid(), clientBoxes);
            }
        } catch (IllegalArgumentException ex) {
            synchronized (playerBoxes) {
                playerBoxes.remove(player.getUuid());
            }
        }

        server.execute(this::informAllPlayers);
    }

    /**
     * Informs all Players about the current boxList
     */
    private void informAllPlayers() {
        PacketByteBuf data = buildS2CPacket();
        for (ServerPlayerEntity player: server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, BlockMeter.S2CPacketIdentifier, data);
        }
    }

    /**
     * Builds a S2CPacket containing the BoxList
     * 
     * @return S2CPacket containing the BoxList
     */
    private PacketByteBuf buildS2CPacket() {
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        synchronized (playerBoxes) {
            data.writeInt(playerBoxes.size());
            Set<Map.Entry<UUID, List<MeasureBox>>> knownPlayers = playerBoxes.entrySet();
            for (Map.Entry<UUID, List<MeasureBox>> playerBoxEntry: knownPlayers) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerBoxEntry.getKey());
                data.writeText(player.getDisplayName());
                data.writeInt(playerBoxEntry.getValue().size());
                for (int i = 0; i < playerBoxEntry.getValue().size(); i++) {
                    playerBoxEntry.getValue().get(i).writePacketBuf(data);
                }
            }
        }
        return data;
    }
}
