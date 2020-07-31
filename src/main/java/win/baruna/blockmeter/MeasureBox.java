package win.baruna.blockmeter;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MeasureBox {

    BlockPos blockStart;
    BlockPos blockEnd;
    Identifier dimension;
    DyeColor color;
    boolean finished;
    int mode;
    int orientation;

    MeasureBox() {};

    public void writePacketBuf(PacketByteBuf buf) {
        buf.writeBlockPos(this.blockStart);
        buf.writeBlockPos(this.blockEnd);
        buf.writeIdentifier(dimension);
        buf.writeInt(color.getId());
        buf.writeBoolean(finished);
        buf.writeInt(mode);
        buf.writeInt(orientation);
    }

    public static MeasureBox fromPacketByteBuf(PacketByteBuf attachedData) {
        MeasureBox result = new MeasureBox();
        result.fillFromPacketByteBuf(attachedData);
        return result;
    }

    void fillFromPacketByteBuf(PacketByteBuf attachedData) {
        blockStart = attachedData.readBlockPos();
        blockEnd = attachedData.readBlockPos();
        dimension = attachedData.readIdentifier();
        color = DyeColor.byId(attachedData.readInt());
        finished = attachedData.readBoolean();
        mode = attachedData.readInt();
        orientation = attachedData.readInt();

        if (Math.abs(blockStart.getX() - blockEnd.getX()) > 1024
                || Math.abs(blockStart.getZ() - blockEnd.getZ()) > 1024
                || blockStart.getY() < 0 || blockStart.getY() > 256
                || blockEnd.getY() < 0 || blockEnd.getY() > 256
                || dimension == null) {
            throw new IllegalArgumentException("invalid buffer");
        }
    }
}
