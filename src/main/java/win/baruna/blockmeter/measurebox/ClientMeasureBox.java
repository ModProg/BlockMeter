package win.baruna.blockmeter.measurebox;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.TextGizmo;
import org.jetbrains.annotations.NotNull;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientMeasureBox extends MeasureBox {

    @NotNull
    public MiningRestriction miningRestriction;
    private Box box;
    private int argb;

    protected ClientMeasureBox(final BlockPos blockStart, final BlockPos blockEnd, final Identifier dimension, final DyeColor color, final boolean finished, final int mode, final int orientation) {
        super(blockStart, blockEnd, dimension, color, finished, mode, orientation);
        miningRestriction = MiningRestriction.Off;
        argb = color.getEntityColor() | 0xFF000000;
        updateBoundingBox();
    }

    public ClientMeasureBox(MeasureBox measureBox) {
        this(measureBox.blockStart, measureBox.blockEnd, measureBox.dimension, measureBox.color, measureBox.finished, measureBox.mode, measureBox.orientation);
    }

    public static ClientMeasureBox getBox(final BlockPos block, final Identifier dimension) {
        final ClientMeasureBox box = new ClientMeasureBox(block, block, dimension, getSelectedColor(), false, 0, 0);
        incrementColor();
        return box;
    }

    /**
     * If enabled increments to next color
     */
    static private void incrementColor() {
        final ModConfig conf = BlockMeterClient.getConfigManager().getConfig();

        if (conf.incrementColor) {
            setColorIndex(conf.colorIndex + 1);
        }
    }

    /**
     * Accessor for the currently selected color
     *
     * @return currently selected color
     */
    static private DyeColor getSelectedColor() {
        final ModConfig conf = BlockMeterClient.getConfigManager().getConfig();
        return DyeColor.byIndex(conf.colorIndex);
    }

    public static void setColorIndex(final int newColor) {
        BlockMeterClient.getConfigManager().getConfig().colorIndex = Math.floorMod(newColor, DyeColor.values().length);
        BlockMeterClient.getConfigManager().save();
    }

    /**
     * Sets the second box corner
     *
     * @param block second corner position
     */
    public void setBlockEnd(final BlockPos block) {
        blockEnd = block;
        updateBoundingBox();
    }

    /**
     * The current creation state of the MeasureBox
     *
     * @return true if MeasureBox is completed
     */
    public boolean isFinished() {
        return this.finished;
    }

    /**
     * Marks Box to be complete
     */
    public void setFinished() {
        this.finished = true;
    }

    /**
     * Sets the Color of the MeasureBox
     *
     * @param color Color to be applied
     */
    public void setColor(final DyeColor color) {
        this.color = color;
        this.argb = color.getEntityColor() | 0xFF000000;
    }

    /**
     * Tests if the block is on a corner of the box
     *
     * @param block Position to test
     * @return true if block is a corner
     */
    public boolean isCorner(final BlockPos block) {
        return (block.getX() == blockStart.getX() || block.getX() == blockEnd.getX()) && (block.getY() == blockStart.getY() || block.getY() == blockEnd.getY()) && (block.getZ() == blockStart.getZ() || block.getZ() == blockEnd.getZ());
    }

    /**
     * Loosens the selected Corner i.e. the opposite corner gets fixed, and the
     * current one can be moved
     *
     * @param block The corner to loosen, needs to be an actual corner of the box
     */
    public void loosenCorner(final BlockPos block) {
        final int x = blockStart.getX() == block.getX() ? blockEnd.getX() : blockStart.getX();
        final int y = blockStart.getY() == block.getY() ? blockEnd.getY() : blockStart.getY();
        final int z = blockStart.getZ() == block.getZ() ? blockEnd.getZ() : blockStart.getZ();
        blockStart = new BlockPos(x, y, z);
        blockEnd = block;
        finished = false;
    }

    public void render(WorldRenderContext context, final Identifier currentDimension) {
        render(context, currentDimension, null);
    }

    public void render(WorldRenderContext context, final Identifier currentDimension, final String boxCreatorName) {
        if (!(currentDimension.equals(this.dimension))) {
            return;
        }

        GizmoDrawing.box(this.box, DrawStyle.stroked(getColor(), 2f)).ignoreOcclusion();

        if (BlockMeterClient.getConfigManager().getConfig().innerDiagonal) {
            GizmoDrawing.line(box.getMinPos(), box.getMaxPos(), getColor()).ignoreOcclusion();
        }

        this.drawLengths(context, boxCreatorName);

    }

    /**
     * Calculates the BoundingBox for rendering
     */
    private void updateBoundingBox() {
        final int ax = this.blockStart.getX();
        final int ay = this.blockStart.getY();
        final int az = this.blockStart.getZ();
        final int bx = this.blockEnd.getX();
        final int by = this.blockEnd.getY();
        final int bz = this.blockEnd.getZ();

        this.box = new Box(Math.min(ax, bx), Math.min(ay, by), Math.min(az, bz), Math.max(ax, bx) + 1, Math.max(ay, by) + 1, Math.max(az, bz) + 1);

    }

    private void drawLengths(WorldRenderContext context, final String boxCreatorName) {
        final int lengthX = (int) this.box.getLengthX();
        final int lengthY = (int) this.box.getLengthY();
        final int lengthZ = (int) this.box.getLengthZ();

        final Vec3d boxCenter = this.box.getCenter();
        final double diagonalLength = new Vec3d(this.box.minX, this.box.minY, this.box.minZ).distanceTo(new Vec3d(this.box.maxX, this.box.maxY, this.box.maxZ));

        final Vec3d pos = context.worldState().cameraRenderState.pos;

        final List<Line> lines = new ArrayList<>();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.minY, this.box.maxZ), pos));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.minX, this.box.maxY, this.box.maxZ), pos));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.maxZ), pos));
        lines.add(new Line(new Box(this.box.maxX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos));
        Collections.sort(lines);
        final Vec3d lineZ = lines.getFirst().line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.maxY, this.box.minZ), pos));
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.minX, this.box.maxY, this.box.maxZ), pos));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ), pos));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos));
        Collections.sort(lines);
        final Vec3d lineY = lines.getFirst().line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.minZ), pos));
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.minY, this.box.maxZ), pos));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ), pos));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos));
        Collections.sort(lines);
        final Vec3d lineX = lines.getFirst().line.getCenter();

        final String playerNameStr = (boxCreatorName == null ? "" : boxCreatorName + ": ");

        if (BlockMeterClient.getConfigManager().getConfig().innerDiagonal) {
            this.drawText(boxCenter, playerNameStr + String.format("%.2f", diagonalLength), pos);
        }
        this.drawText(lineZ, playerNameStr + lengthZ, pos);
        this.drawText(lineX, playerNameStr + lengthX, pos);
        this.drawText(lineY, playerNameStr + lengthY, pos);
    }

    private void drawText(Vec3d pos, final String text, final Vec3d playerPos) {
        float size = .3f;
        final int constDist = 10;

        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().minimalLabelSize) {
            var dist = (float) pos.distanceTo(playerPos);
            if (dist > constDist) size = dist * size / constDist;
        }

        GizmoDrawing.text(text, pos, TextGizmo.Style.left(getColor()).scaled(size)).ignoreOcclusion();
    }

    public boolean contains(BlockPos block) {
        return BlockBox.create(blockStart, blockEnd).contains(block);
    }

    public enum MiningRestriction {
        Off("options.off"), Inside("blockmeter.restrictMining.inside"), Outside("blockmeter.restrictMining.outside");
        public final String translation;

        MiningRestriction(String translation) {
            this.translation = translation;
        }

        @NotNull
        public MiningRestriction next() {
            switch (this) {
                case Off -> {
                    return Inside;
                }
                case Inside -> {
                    return Outside;
                }
                case Outside -> {
                    return Off;
                }
                default -> throw new IllegalArgumentException();
            }
        }
    }

    private static class Line implements Comparable<Line> {
        Box line;
        double distance;

        Line(final Box line, final Vec3d pos) {
            this.line = line;
            this.distance = line.getCenter().distanceTo(pos);
        }

        @Override
        public int compareTo(final Line l) {
            return Double.compare(this.distance, l.distance);
        }
    }
}
