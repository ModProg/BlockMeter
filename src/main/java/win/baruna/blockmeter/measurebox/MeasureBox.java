package win.baruna.blockmeter.measurebox;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MeasureBox {

    protected BlockPos blockStart;
    protected BlockPos blockEnd;
    protected Identifier dimension;
    protected DyeColor color;
    protected boolean finished;
    protected int mode;
    protected int orientation;

    public BlockPos getBlockStart() {
        return blockStart;
    }

    public BlockPos getBlockEnd() {
        return blockEnd;
    }

    public Identifier getDimension() {
        return dimension;
    }

    public DyeColor getColor() {
        return color;
    }

    public boolean isFinished() {
        return finished;
    }

    protected MeasureBox(BlockPos blockStart, BlockPos blockEnd,
            Identifier dimension, DyeColor color, boolean finished, int mode,
            int orientation) {
        this.blockStart = blockStart;
        this.blockEnd = blockEnd;
        this.dimension = dimension;
        this.color = color;
        this.finished = finished;
        this.mode = mode;
        this.orientation = orientation;
    }

    /**
     * Creates a MeasureBox from a PacketByteBuf
     * 
     * @param attachedData a PacketByteBuf containing the ClientMeasureBox
     * @return the PacketByteBuf submitted
     */
    protected MeasureBox(PacketByteBuf attachedData) {
        this.blockStart = attachedData.readBlockPos();
        this.blockEnd = attachedData.readBlockPos();
        this.dimension = attachedData.readIdentifier();
        this.color = DyeColor.byId(attachedData.readInt());
        this.finished = attachedData.readBoolean();
        this.mode = attachedData.readInt();
        this.orientation = attachedData.readInt();

        if (blockStart.getY() < 0 || blockStart.getY() > 256
                || blockEnd.getY() < 0 || blockEnd.getY() > 256
                || dimension == null) {
            throw new IllegalArgumentException("invalid buffer");
        }
    }

    /**
     * Fills a PacketByteBuf with the MeasureBox
     * 
     * @param buf PacketByteBuf to fill
     */
    public void writePacketBuf(PacketByteBuf buf) {
        buf.writeBlockPos(this.blockStart);
        buf.writeBlockPos(this.blockEnd);
        buf.writeIdentifier(dimension);
        buf.writeInt(color.getId());
        buf.writeBoolean(finished);
        buf.writeInt(mode);
        buf.writeInt(orientation);
    }

    /**
     * Parses a MeasureBox from a PacketByteBuf
     * 
     * @param attachedData a PacketByteBuf containing the ClientMeasureBox
     * @return the PacketByteBuf submitted
     */
    public static MeasureBox fromPacketByteBuf(PacketByteBuf attachedData) {
        return new MeasureBox(attachedData);
    }
}
