package win.baruna.blockmeter.measurebox;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientMeasureBox extends MeasureBox {
    private Box box;
    private int argb;

    @NotNull
    public MiningRestriction miningRestriction;

    protected ClientMeasureBox(final BlockPos blockStart, final BlockPos blockEnd, final Identifier dimension,
                               final DyeColor color, final boolean finished, final int mode, final int orientation) {
        super(blockStart, blockEnd, dimension, color, finished, mode, orientation);
        miningRestriction = MiningRestriction.Off;
        argb = color.getEntityColor() | 0xFF000000;
        updateBoundingBox();
    }

    public ClientMeasureBox(MeasureBox measureBox) {
        this(measureBox.blockStart, measureBox.blockEnd, measureBox.dimension, measureBox.color, measureBox.finished,
                measureBox.mode, measureBox.orientation);
    }

    public static ClientMeasureBox getBox(final BlockPos block, final Identifier dimension) {
        final ClientMeasureBox box = new ClientMeasureBox(block, block, dimension, getSelectedColor(), false, 0, 0);
        incrementColor();
        return box;
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
        return (block.getX() == blockStart.getX() || block.getX() == blockEnd.getX())
                && (block.getY() == blockStart.getY() || block.getY() == blockEnd.getY())
                && (block.getZ() == blockStart.getZ() || block.getZ() == blockEnd.getZ());
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

    public void render(final WorldRenderContext context, final Identifier currentDimension) {
        render(context, currentDimension, null);
    }

    public void render(final WorldRenderContext context, final Identifier currentDimension,
                       final Text boxCreatorName) {
        if (!(currentDimension.equals(this.dimension))) {
            return;
        }
        final Vec3d pos = context.camera().getPos();
        var stack = context.matrixStack();

        // FIXME This actually does nothing
        RenderSystem.lineWidth(2.0f);

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        stack.push();
        stack.translate(-pos.x, -pos.y, -pos.z);
        final Matrix4f model = stack.peek().getPositionMatrix();

        final Tessellator tess = Tessellator.getInstance();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = tess.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.minZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(argb);
        ;
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(argb);
        ;
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.minZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(argb);
        ;
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.maxZ).color(argb);
        ;
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.maxZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(argb);
        ;
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.minZ).color(argb);
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.minZ).color(argb);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if (BlockMeterClient.getConfigManager().getConfig().innerDiagonal) {
            buffer = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(argb);
            buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(argb);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
        RenderSystem.lineWidth(1.0f);

        this.drawLengths(context, boxCreatorName);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        stack.pop();
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

        this.box = new Box(Math.min(ax, bx), Math.min(ay, by), Math.min(az, bz),
                Math.max(ax, bx) + 1, Math.max(ay, by) + 1, Math.max(az, bz) + 1);

    }

    private void drawLengths(final WorldRenderContext context, final Text boxCreatorName) {
        final int lengthX = (int) this.box.getLengthX();
        final int lengthY = (int) this.box.getLengthY();
        final int lengthZ = (int) this.box.getLengthZ();

        final Vec3d boxCenter = this.box.getCenter();
        final double diagonalLength = new Vec3d(this.box.minX, this.box.minY, this.box.minZ)
                .distanceTo(new Vec3d(this.box.maxX, this.box.maxY, this.box.maxZ));

        var camera = context.camera();
        final float yaw = camera.getYaw();
        final float pitch = camera.getPitch();
        final Vec3d pos = camera.getPos();

        final List<Line> lines = new ArrayList<>();
        lines.add(new Line(
                new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.minY, this.box.maxZ),
                pos));
        lines.add(new Line(
                new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.minX, this.box.maxY, this.box.maxZ),
                pos));
        lines.add(new Line(
                new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.maxZ),
                pos));
        lines.add(new Line(
                new Box(this.box.maxX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.maxZ),
                pos));
        Collections.sort(lines);
        final Vec3d lineZ = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(
                new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.maxY, this.box.minZ),
                pos));
        lines.add(new Line(
                new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.minX, this.box.maxY, this.box.maxZ),
                pos));
        lines.add(new Line(
                new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ),
                pos));
        lines.add(new Line(
                new Box(this.box.maxX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ),
                pos));
        Collections.sort(lines);
        final Vec3d lineY = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(
                new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.minZ),
                pos));
        lines.add(new Line(
                new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.minY, this.box.maxZ),
                pos));
        lines.add(new Line(
                new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ),
                pos));
        lines.add(new Line(
                new Box(this.box.minX, this.box.maxY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ),
                pos));
        Collections.sort(lines);
        final Vec3d lineX = lines.get(0).line.getCenter();

        final String playerNameStr = (boxCreatorName == null ? "" : boxCreatorName.getString() + " : ");

        if (BlockMeterClient.getConfigManager().getConfig().innerDiagonal) {
            this.drawBackground(context, boxCenter.x, boxCenter.y, boxCenter.z, yaw, pitch,
                    playerNameStr + String.format("%.2f", diagonalLength), pos);
        }
        this.drawBackground(context, lineZ.x, lineZ.y, lineZ.z, yaw, pitch, playerNameStr + lengthZ, pos);
        this.drawBackground(context, lineX.x, lineX.y, lineX.z, yaw, pitch, playerNameStr + lengthX, pos);
        this.drawBackground(context, lineY.x, lineY.y, lineY.z, yaw, pitch, playerNameStr + lengthY, pos);

        if (BlockMeterClient.getConfigManager().getConfig().innerDiagonal) {
            this.drawText(context, boxCenter.x, boxCenter.y, boxCenter.z, yaw, pitch,
                    playerNameStr + String.format("%.2f", diagonalLength), pos);
        }
        this.drawText(context, lineZ.x, lineZ.y, lineZ.z, yaw, pitch, playerNameStr + lengthZ, pos);
        this.drawText(context, lineX.x, lineX.y, lineX.z, yaw, pitch, playerNameStr + lengthX, pos);
        this.drawText(context, lineY.x, lineY.y, lineY.z, yaw, pitch, playerNameStr + lengthY, pos);
    }

    private void drawBackground(final WorldRenderContext context, final double x, final double y, final double z,
                                final float yaw,
                                final float pitch, final String text, final Vec3d playerPos) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final var literalText = Text.literal(text);

        float size = 0.03f;
        final int constDist = 10;

        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().minimalLabelSize) {
            final float dist = (float) Math.sqrt((x - playerPos.x) * (x - playerPos.x)
                    + (y - playerPos.y) * (y - playerPos.y) + (z - playerPos.z) * (z - playerPos.z));
            if (dist > constDist)
                size = dist * size / constDist;
        }

        var stack = context.matrixStack();
        stack.push();
        stack.translate(x, y + 0.15, z);
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
        stack.scale(size, -size, 0.001f);
        final int width = textRenderer.getWidth(literalText);
        stack.translate((-width / 2f), 0.0, 0.0);
        final Matrix4f model = stack.peek().getPositionMatrix();

        final ModConfig conf = BlockMeterClient.getConfigManager().getConfig();
        if (conf.backgroundForLabels) {
            var buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(model, -1, -1, 0).color(argb);
            buffer.vertex(model, -1, 8, 0).color(argb);
            buffer.vertex(model, width, 8, 0).color(argb);
            buffer.vertex(model, width, -1, 0).color(argb);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        stack.pop();
    }

    private void drawText(final WorldRenderContext context, final double x, final double y, final double z,
                          final float yaw,
                          final float pitch, final String text, final Vec3d playerPos) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final var literalText = Text.literal(text);

        float size = 0.03f;
        final int constDist = 10;

        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().minimalLabelSize) {
            final float dist = (float) Math.sqrt((x - playerPos.x) * (x - playerPos.x)
                    + (y - playerPos.y) * (y - playerPos.y) + (z - playerPos.z) * (z - playerPos.z));
            if (dist > constDist)
                size = dist * size / constDist;
        }

        var stack = context.matrixStack();
        stack.push();
        stack.translate(x, y + 0.15, z);
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
        stack.scale(size, -size, 0.001f);
        final int width = textRenderer.getWidth(literalText);
        stack.translate((-width / 2f), 0.0, 0.0);
        final Matrix4f model = stack.peek().getPositionMatrix();

        int textColor = color.getSignColor();

        final ModConfig conf = BlockMeterClient.getConfigManager().getConfig();
        if (conf.backgroundForLabels) {
            var color = new Color(argb);
            float luminance = (0.299f * color.getRed() + 0.587f * color.getGreen() + 0.114f * color.getBlue());
            textColor = luminance < 0.4f ? DyeColor.WHITE.getSignColor() : DyeColor.BLACK.getSignColor();
        }

        final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferAllocator(0));
        textRenderer.draw(
                literalText,
                0.0f,
                0.0f,
                textColor,
                !conf.backgroundForLabels, // shadow
                model, // matrix
                immediate, // draw buffer
                TextRenderer.TextLayerType.SEE_THROUGH,
                0, // backgroundColor => underlineColor,
                15728880 // light
        );
        immediate.draw();

        stack.pop();
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
        return DyeColor.byId(conf.colorIndex);
    }

    public static void setColorIndex(final int newColor) {
        BlockMeterClient.getConfigManager().getConfig().colorIndex = Math.floorMod(newColor, DyeColor.values().length);
        BlockMeterClient.getConfigManager().save();
    }

    public boolean contains(BlockPos block) {
        return BlockBox.create(blockStart, blockEnd).contains(block);
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

    public enum MiningRestriction {
        Off("options.off"),
        Inside("blockmeter.restrictMining.inside"),
        Outside("blockmeter.restrictMining.outside");
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
}
