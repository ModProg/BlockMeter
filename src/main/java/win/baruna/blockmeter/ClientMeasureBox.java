package win.baruna.blockmeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class ClientMeasureBox extends MeasureBox {
    Box box;

    private ClientMeasureBox() {
        super();
    }

    ClientMeasureBox(final BlockPos block, final Identifier dimension) {
        this.blockStart = block;
        this.blockEnd = block;
        this.dimension = dimension;
        this.color = getNextColor();
        this.finished = false;
        this.setBoundingBox();
    }

    public static ClientMeasureBox fromPacketByteBuf(PacketByteBuf attachedData) {
        ClientMeasureBox result = new ClientMeasureBox();
        result.fillFromPacketByteBuf(attachedData);
        result.setBoundingBox();
        return result;
    }

    public void setBlockEnd(BlockPos pos) {
        blockEnd = pos;
        setBoundingBox();
    }

    public static void selectColorIndex(int newColor) {
        BlockMeterClient.confmgr.getConfig().colorIndex = Math.floorMod(newColor, DyeColor.values().length);
        BlockMeterClient.confmgr.save();
    }

    private void setBoundingBox() {
        final int ax = this.blockStart.getX();
        final int ay = this.blockStart.getY();
        final int az = this.blockStart.getZ();
        final int bx = this.blockEnd.getX();
        final int by = this.blockEnd.getY();
        final int bz = this.blockEnd.getZ();

        this.box = new Box(
                (double) Math.min(ax, bx), (double) Math.min(ay, by),
                (double) Math.min(az, bz), (double) (Math.max(ax, bx) + 1),
                (double) (Math.max(ay, by) + 1),
                (double) (Math.max(az, bz) + 1));

    }

    void render(Camera camera, MatrixStack stack, Identifier currentDimension) {
        render(camera, stack, currentDimension, null);
    }

    void render(final Camera camera, MatrixStack stack, final Identifier currentDimension, Text playerName) {
        if (!(currentDimension.equals(this.dimension))) {
            return;
        }
        final Vec3d pos = camera.getPos();

        RenderSystem.lineWidth(2.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        stack.push();
        stack.translate(-pos.x, -pos.y, -pos.z);
        Matrix4f model = stack.peek().getModel();

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buffer = tess.getBuffer();
        final float[] color = this.color.getColorComponents();
        final float r = color[0];
        final float g = color[1];
        final float b = color[2];
        final float a = 0.8f;

        buffer.begin(3, VertexFormats.POSITION_COLOR);

        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.minZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(r, g, b, a).next();

        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(r, g, b, a).next();

        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.minZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.minZ).color(r, g, b, a).next();

        buffer.vertex(model, (float) this.box.minX, (float) this.box.maxY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.maxZ).color(r, g, b, a).next();

        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.maxZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(r, g, b, a).next();

        buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.minZ).color(r, g, b, a).next();
        buffer.vertex(model, (float) this.box.maxX, (float) this.box.minY, (float) this.box.minZ).color(r, g, b, a).next();
        tess.draw();

        if (BlockMeterClient.confmgr.getConfig().innerDiagonal) {
            buffer.begin(1, VertexFormats.POSITION_COLOR);
            buffer.vertex(model, (float) this.box.minX, (float) this.box.minY, (float) this.box.minZ).color(r, g, b, a).next();
            buffer.vertex(model, (float) this.box.maxX, (float) this.box.maxY, (float) this.box.maxZ).color(r, g, b, a).next();
            tess.draw();

        }
        RenderSystem.enableTexture();
        RenderSystem.lineWidth(1.0f);

        this.drawLengths(camera, stack, playerName);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    private void drawLengths(final Camera camera, MatrixStack stack, final Text playerName) {
        final int lengthX = (int) this.box.getXLength();
        final int lengthY = (int) this.box.getYLength();
        final int lengthZ = (int) this.box.getZLength();

        final Vec3d boxCenter = this.box.getCenter();
        final double diagonalLength = new Vec3d(this.box.minX, this.box.minY, this.box.minZ).distanceTo(new Vec3d(this.box.maxX, this.box.maxY, this.box.maxZ));

        final float yaw = camera.getYaw();
        final float pitch = camera.getPitch();
        final Vec3d pos = camera.getPos();

        final Frustum frustum = new Frustum(stack.peek().getModel(), AffineTransformation.identity().getMatrix());
        frustum.setPosition(pos.x, pos.y, pos.z);

        final List<Line> lines = new ArrayList<>();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.minY, this.box.maxZ), pos, frustum));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.minX, this.box.maxY, this.box.maxZ), pos, frustum));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.maxZ), pos, frustum));
        lines.add(new Line(new Box(this.box.maxX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineZ = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.minX, this.box.maxY, this.box.minZ), pos, frustum));
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.minX, this.box.maxY, this.box.maxZ), pos, frustum));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ), pos, frustum));
        lines.add(new Line(new Box(this.box.maxX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineY = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.minZ, this.box.maxX, this.box.minY, this.box.minZ), pos, frustum));
        lines.add(new Line(new Box(this.box.minX, this.box.minY, this.box.maxZ, this.box.maxX, this.box.minY, this.box.maxZ), pos, frustum));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.minZ, this.box.maxX, this.box.maxY, this.box.minZ), pos, frustum));
        lines.add(new Line(new Box(this.box.minX, this.box.maxY, this.box.maxZ, this.box.maxX, this.box.maxY, this.box.maxZ), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineX = lines.get(0).line.getCenter();

        String playerNameStr = (playerName == null ? "" : playerName.getString() + " : ");
        if (BlockMeterClient.confmgr.getConfig().innerDiagonal) {
            this.drawText(stack, boxCenter.x, boxCenter.y, boxCenter.z, yaw, pitch, playerNameStr + String.format("%.2f", diagonalLength), pos);
        }
        this.drawText(stack, lineX.x, lineX.y, lineX.z, yaw, pitch, playerNameStr + String.valueOf(lengthX), pos);
        this.drawText(stack, lineY.x, lineY.y, lineY.z, yaw, pitch, playerNameStr + String.valueOf(lengthY), pos);
        this.drawText(stack, lineZ.x, lineZ.y, lineZ.z, yaw, pitch, playerNameStr + String.valueOf(lengthZ), pos);
    }

    private void drawText(MatrixStack stack, final double x, final double y, final double z, final float yaw, final float pitch, final String text, final Vec3d playerPos) {
        @SuppressWarnings("resource")
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final LiteralText literalText = new LiteralText(text);

        float size = 0.03f;
        final int constDist = 10;

        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().minimalLabelSize) {
            final float dist = (float) Math.sqrt((x - playerPos.x) * (x - playerPos.x) + (y - playerPos.y) * (y - playerPos.y) + (z - playerPos.z) * (z - playerPos.z));
            if (dist > constDist)
                size = dist * size / constDist;
        }

        stack.push();
        stack.translate(x, y + 0.15, z);
        stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - yaw));
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-pitch));
        stack.scale(size, -size, 0.001f);
        int width = textRenderer.getWidth(literalText);
        stack.translate((-width / 2), 0.0, 0.0);
        Matrix4f model = stack.peek().getModel();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        float[] colors = this.color.getColorComponents();
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(model, -1, -1, 0).color(colors[0], colors[1], colors[2], 0.8f).next();
        buffer.vertex(model, -1, 8, 0).color(colors[0], colors[1], colors[2], 0.8f).next();
        buffer.vertex(model, width, 8, 0).color(colors[0], colors[1], colors[2], 0.8f).next();
        buffer.vertex(model, width, -1, 0).color(colors[0], colors[1], colors[2], 0.8f).next();
        Tessellator.getInstance().draw();

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);
        textRenderer.draw(literalText, 0.0f, 0.0f,
                this.color.getSignColor(),
                false,                              // shadow
                model,                              // matrix
                immediate,                          // draw buffer
                true,                               // seeThrough
                0,                                  // backgroundColor => underlineColor,
                0                                   // light
        );
        immediate.draw();

        stack.pop();
    }

    /**
     * If enabled increments to next color
     * @return currently selected Color
     */
    static private DyeColor getNextColor() {
        ModConfig conf = BlockMeterClient.confmgr.getConfig();

        final DyeColor selectedColor = DyeColor.byId(conf.colorIndex);

        if (conf.incrementColor) {
            selectColorIndex(conf.colorIndex + 1);
        }

        return selectedColor;
    }

    boolean isFinished() {
        return this.finished;
    }

    void setFinished() {
        this.finished = true;
    }

    private class Line implements Comparable<Line> {
        Box line;
        boolean isVisible;
        double distance;

        Line(final Box line, final Vec3d pos, final Frustum frustum) {
            this.line = line;
            this.isVisible = frustum.isVisible(line);
            this.distance = line.getCenter().distanceTo(pos);
        }

        @Override
        public int compareTo(final Line l) {
            if (this.isVisible) {
                return l.isVisible ? Double.compare(this.distance, l.distance) : -1;
            }
            return l.isVisible ? 1 : 0;
        }
    }

    private class Frustum {
        Frustum(Matrix4f a, Matrix4f b) {}

        void setPosition(double a, double b, double c) {}

        boolean isVisible(Box line) {
            return true;
        }
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }
}
