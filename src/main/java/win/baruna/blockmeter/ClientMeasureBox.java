//   // Decompiled by Procyon v0.5.30
// 
package win.baruna.blockmeter;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class ClientMeasureBox extends MeasureBox
{
    Box box;
    public static int colorIndex;
    public static boolean incrementColor;
    public static boolean innerDiagonal;
    
    private ClientMeasureBox() {
        super();
    }

    ClientMeasureBox(final BlockPos block, final DimensionType dimension) {
        this.blockStart = block;
        this.blockEnd = block;
        this.dimension = dimension;
        this.color = this.getNextColor();
        this.finished = false;
        this.setBoundingBox();        setBoundingBox();
    }

    public static ClientMeasureBox fromPacketByteBuf(PacketByteBuf attachedData) {
        ClientMeasureBox result = new ClientMeasureBox();
        result.fillFromPacketByteBuf(attachedData);
        result.setBoundingBox();
        return result;
    }
    
    public void setBlockEnd(BlockPos pos) {
        blockEnd=pos;
        setBoundingBox();
    }
    
    public static void selectColorIndex(int newColor) {
        colorIndex=newColor;
    }

    private void setBoundingBox() {
        final int ax = this.blockStart.getX();
        final int ay = this.blockStart.getY();
        final int az = this.blockStart.getZ();
        final int bx = this.blockEnd.getX();
        final int by = this.blockEnd.getY();
        final int bz = this.blockEnd.getZ();

        this.box = new Box((double)Math.min(ax, bx), (double)Math.min(ay, by), (double)Math.min(az, bz), (double)(Math.max(ax, bx) + 1), (double)(Math.max(ay, by) + 1), (double)(Math.max(az, bz) + 1));

    }
    
    void render(Camera camera, MatrixStack stack, DimensionType currentDimension) {
        render(camera, stack, currentDimension, null);
    }
    
    void render(final Camera camera, MatrixStack stack, final DimensionType currentDimension, Text playerName) {
        if (currentDimension != this.dimension) {
            return;          }
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

        buffer.vertex(model, (float)this.box.x1, (float)this.box.y1, (float)this.box.z1).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y1, (float)this.box.z1).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y1, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x1, (float)this.box.y1, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x1, (float)this.box.y1, (float)this.box.z1).color(r, g, b, 0.8f).next();

        buffer.vertex(model, (float)this.box.x1, (float)this.box.y2, (float)this.box.z1).color(r, g, b, 0.8f).next();

        buffer.vertex(model, (float)this.box.x1, (float)this.box.y2, (float)this.box.z1).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y2, (float)this.box.z1).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y2, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x1, (float)this.box.y2, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x1, (float)this.box.y2, (float)this.box.z1).color(r, g, b, 0.8f).next();

        buffer.vertex(model, (float)this.box.x1, (float)this.box.y2, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x1, (float)this.box.y1, (float)this.box.z2).color(r, g, b, 0.8f).next();

        buffer.vertex(model, (float)this.box.x2, (float)this.box.y1, (float)this.box.z2).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y2, (float)this.box.z2).color(r, g, b, 0.8f).next();

        buffer.vertex(model, (float)this.box.x2, (float)this.box.y2, (float)this.box.z1).color(r, g, b, 0.8f).next();
        buffer.vertex(model, (float)this.box.x2, (float)this.box.y1, (float)this.box.z1).color(r, g, b, 0.8f).next();
        tess.draw();

        if (ClientMeasureBox.innerDiagonal) {
            buffer.begin(1, VertexFormats.POSITION_COLOR);
            buffer.vertex(model, (float)this.box.x1, (float)this.box.y1, (float)this.box.z1).color(r, g, b, 0.8f).next();
            buffer.vertex(model, (float)this.box.x2, (float)this.box.y2, (float)this.box.z2).color(r, g, b, 0.8f).next();
            tess.draw();

        }
        RenderSystem.enableTexture();
        RenderSystem.lineWidth(1.0f);

        this.drawLength(camera, stack, playerName);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        stack.pop();
    }
    
    private void drawLength(final Camera camera, MatrixStack stack, final Text playerName) {
        final int lengthX = (int)this.box.getXLength();
        final int lengthY = (int)this.box.getYLength();
        final int lengthZ = (int)this.box.getZLength();

        final Vec3d boxCenter = this.box.getCenter();
        final double diagonalLength = new Vec3d(this.box.x1, this.box.y1, this.box.z1).distanceTo(new Vec3d(this.box.x2, this.box.y2, this.box.z2));


        final float yaw = camera.getYaw();
        final float pitch = camera.getPitch();
        final Vec3d pos = camera.getPos();

        final Frustum frustum = new Frustum();
        frustum.setOrigin(pos.x, pos.y, pos.z);

        final List<Line> lines = new ArrayList<>();
        lines.add(new Line(new Box(this.box.x1, this.box.y1, this.box.z1, this.box.x1, this.box.y1, this.box.z2), pos, frustum));
        lines.add(new Line(new Box(this.box.x1, this.box.y2, this.box.z1, this.box.x1, this.box.y2, this.box.z2), pos, frustum));
        lines.add(new Line(new Box(this.box.x2, this.box.y1, this.box.z1, this.box.x2, this.box.y1, this.box.z2), pos, frustum));
        lines.add(new Line(new Box(this.box.x2, this.box.y2, this.box.z1, this.box.x2, this.box.y2, this.box.z2), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineZ = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.x1, this.box.y1, this.box.z1, this.box.x1, this.box.y2, this.box.z1), pos, frustum));
        lines.add(new Line(new Box(this.box.x1, this.box.y1, this.box.z2, this.box.x1, this.box.y2, this.box.z2), pos, frustum));
        lines.add(new Line(new Box(this.box.x2, this.box.y1, this.box.z1, this.box.x2, this.box.y2, this.box.z1), pos, frustum));
        lines.add(new Line(new Box(this.box.x2, this.box.y1, this.box.z2, this.box.x2, this.box.y2, this.box.z2), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineY = lines.get(0).line.getCenter();

        lines.clear();
        lines.add(new Line(new Box(this.box.x1, this.box.y1, this.box.z1, this.box.x2, this.box.y1, this.box.z1), pos, frustum));
        lines.add(new Line(new Box(this.box.x1, this.box.y1, this.box.z2, this.box.x2, this.box.y1, this.box.z2), pos, frustum));
        lines.add(new Line(new Box(this.box.x1, this.box.y2, this.box.z1, this.box.x2, this.box.y2, this.box.z1), pos, frustum));
        lines.add(new Line(new Box(this.box.x1, this.box.y2, this.box.z2, this.box.x2, this.box.y2, this.box.z2), pos, frustum));
        Collections.sort(lines);
        final Vec3d lineX = lines.get(0).line.getCenter();

        String playerNameStr = (playerName == null ? "" : playerName.getString()+" : ");
        if (ClientMeasureBox.innerDiagonal) {
            this.drawText(stack, boxCenter.x, boxCenter.y, boxCenter.z, yaw, pitch, playerNameStr+String.format("%.2f", diagonalLength));
        }
        this.drawText(stack, lineX.x, lineX.y, lineX.z, yaw, pitch, playerNameStr+String.valueOf(lengthX));
        this.drawText(stack, lineY.x, lineY.y, lineY.z, yaw, pitch, playerNameStr+String.valueOf(lengthY));
        this.drawText(stack, lineZ.x, lineZ.y, lineZ.z, yaw, pitch, playerNameStr+String.valueOf(lengthZ));
    }
    
    private void drawText(MatrixStack stack, final double x, final double y, final double z, final float yaw, final float pitch, final String length) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final String lengthString = String.valueOf(length);

        final float size = 0.03f;

        stack.push();
        stack.translate(x, y + 0.15, z);
        stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - yaw));
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-pitch));
        // stack.multiply(new Quaternion((float)(180.0f - yaw), 0.0f, 1.0f, 0.0f));
        // stack.multiply(new Quaternion((float)(-pitch), 1.0f, 0.0f, 0.0f));
        stack.scale(0.03f, -0.03f, 0.001f);
        stack.translate((-textRenderer.getStringWidth(lengthString) / 2), 0.0, 0.0);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(new LiteralText(lengthString), 0.0f, 0.0f, 
                this.color.getSignColor(),
                false,                              // shadow
                stack.peek().getModel(),            // matrix
                immediate,                          // draw buffer
                true,                               // seeThrough
                0,                                  // backgroundColor => underlineColor,
                0x0                                 // light
        );
        immediate.draw();
        stack.pop();
    }
    
    private DyeColor getNextColor() {
        final DyeColor selectedColor = DyeColor.byId(ClientMeasureBox.colorIndex);

        if (ClientMeasureBox.incrementColor) {

            ++ClientMeasureBox.colorIndex;
        }
        if (ClientMeasureBox.colorIndex >= DyeColor.values().length) {
            ClientMeasureBox.colorIndex = 0;

        }
        return selectedColor;
    }
    
    boolean isFinished() {
        return this.finished;
    }
    
    void setFinished() {
        this.finished = true;
    }      static {          ClientMeasureBox.colorIndex = -1;          ClientMeasureBox.incrementColor = true;
        ClientMeasureBox.innerDiagonal = false;
    }

    private class Line implements Comparable<Line>
    {
        Box line;
        boolean isVisible;
        double distance;
        Line(final Box line, final Vec3d pos, final Frustum frustum) {
            this.line = line;
            this.isVisible = frustum.intersects(line);
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
        void setOrigin(double a, double b, double c) {}
        boolean intersects(Box line) { return true; }
    }
}
