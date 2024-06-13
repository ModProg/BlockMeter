package win.baruna.blockmeter;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import win.baruna.blockmeter.measurebox.MeasureBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BoxPayload(Map<String, List<MeasureBox>> receivedBoxes) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, BoxPayload> CODEC =
            CustomPayload.codecOf(BoxPayload::write, BoxPayload::new);

    private void write(PacketByteBuf data) {
        data.writeInt(receivedBoxes.size());
        for (var player : receivedBoxes.entrySet()) {
            data.writeString(player.getKey());
            data.writeInt(player.getValue().size());
            for (int i = 0; i < player.getValue().size(); i++) {
                player.getValue().get(i).writePacketBuf(data);
            }
        }
    }

    public static final Id<BoxPayload> ID = CustomPayload.id(BlockMeter.MOD_ID + ":boxes");

    public BoxPayload(PacketByteBuf data) {
        this(new HashMap<>());

        var playerCount = data.readInt();
        for (var i = 0; i < playerCount; i++) {
            var playerName = data.readString();
            int boxCount = data.readInt();
            var boxes = new ArrayList<MeasureBox>(boxCount);
            for (var j = 0; j < boxCount; j++) {
                boxes.add(MeasureBox.fromPacketByteBuf(data));
            }
            receivedBoxes.put(playerName, boxes);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
