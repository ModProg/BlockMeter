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
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class BlockMeterServer implements ModInitializer {

    private Map<UUID, List<MeasureBox>> playerBoxes;
    private MinecraftServer server;
    private static BlockMeterServer instance;

    @Override
    public void onInitialize() {

        instance = this;
        playerBoxes = new HashMap<>();
        ServerSidePacketRegistry.INSTANCE.register(BlockMeter.C2SPacketIdentifier,
                (packetContext, attachedData) -> {
                    processClientPacket(packetContext, attachedData);
                });
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);
    }

    public static void removePlayer(ServerPlayerEntity player) {
        if (instance != null) {
            if (instance.playerBoxes.containsKey(player.getUuid())) {
                instance.playerBoxes.remove(player.getUuid());
                instance.informAllPlayers();
            }
        }
    }

    private void onStartServer(MinecraftServer server) {
        this.server = server;
    }

    private void processClientPacket(PacketContext packetContext, PacketByteBuf attachedData) {
        int size = attachedData.readInt();

        try {
            List<MeasureBox> clientBoxes = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                clientBoxes.add(MeasureBox.fromPacketByteBuf(attachedData));
            }
            synchronized (playerBoxes) {
                playerBoxes.put(packetContext.getPlayer().getUuid(), clientBoxes);
            }
        } catch (IllegalArgumentException ex) {
            synchronized (playerBoxes) {
                playerBoxes.remove(packetContext.getPlayer().getUuid());
            }
        }

        packetContext.getTaskQueue().execute(() -> informAllPlayers());
    }

    private void informAllPlayers() {
        PacketByteBuf data = buildS2CPacket();
        for (ServerPlayerEntity player: server.getPlayerManager().getPlayerList()) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, BlockMeter.S2CPacketIdentifier, data);
        }
    }

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
