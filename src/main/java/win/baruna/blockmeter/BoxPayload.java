package win.baruna.blockmeter;

import win.baruna.blockmeter.measurebox.MeasureBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BoxPayload(Map<String, List<MeasureBox>> receivedBoxes) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, BoxPayload> CODEC =
            CustomPacketPayload.codec(BoxPayload::write, BoxPayload::new);

    private void write(FriendlyByteBuf data) {
        data.writeInt(receivedBoxes.size());
        for (var player : receivedBoxes.entrySet()) {
            data.writeUtf(player.getKey());
            data.writeInt(player.getValue().size());
            for (int i = 0; i < player.getValue().size(); i++) {
                player.getValue().get(i).writePacketBuf(data);
            }
        }
    }

    public static final Type<BoxPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(BlockMeter.MOD_ID, "boxes"));

    public BoxPayload(FriendlyByteBuf data) {
        this(new HashMap<>());

        var playerCount = data.readInt();
        for (var i = 0; i < playerCount; i++) {
            var playerName = data.readUtf();
            int boxCount = data.readInt();
            var boxes = new ArrayList<MeasureBox>(boxCount);
            for (var j = 0; j < boxCount; j++) {
                boxes.add(MeasureBox.fromPacketByteBuf(data));
            }
            receivedBoxes.put(playerName, boxes);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
