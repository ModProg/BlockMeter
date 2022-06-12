package win.baruna.blockmeter.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class OptionsGui extends Screen {

    public OptionsGui() {
        super(NarratorManager.EMPTY);
    }

    private final static int BUTTONWIDTH = 200;

    @Override
    protected void init() {
        ModConfig config = BlockMeterClient.getConfigManager().getConfig();

        // Create Color Selector
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                final int colorIndex = i * 4 + j;
                this.addDrawableChild(new ColorButton(this.width / 2 - 44 + j * 22,
                        this.height / 2 - 88 + i * 22, 20, 20, null,
                        DyeColor.byId(colorIndex).getColorComponents(), config.colorIndex == colorIndex, false,
                        button -> {
                            ClientMeasureBox.setColorIndex(colorIndex);

                            final ClientMeasureBox currentBox = BlockMeterClient.getInstance().getCurrentBox();
                            if (currentBox != null)
                                currentBox.setColor(DyeColor.byId(colorIndex));
                            MinecraftClient.getInstance().setScreen((Screen) null);
                        }));
            }
        }

        this.addDrawableChild(new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 10,
                BUTTONWIDTH, 20,
                Text.translatable("blockmeter.keepColor", new Object[] {
                        Text.translatable(config.incrementColor ? "options.off" : "options.on")
                }), button -> {
                    config.incrementColor = !config.incrementColor;
                    MinecraftClient.getInstance().setScreen((Screen) null);
                    // Todo find a way to increment to a new Color if a box was created while
                    // incrementColor was disabled
                    BlockMeterClient.getConfigManager().save();
                }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 32,
                BUTTONWIDTH, 20,
                Text.translatable("blockmeter.diagonal", new Object[] {
                        Text.translatable(config.innerDiagonal ? "options.on" : "options.off")
                }), button -> {
                    System.err.println("IDK WHAT YOU ARE DOING");
                    config.innerDiagonal = !config.innerDiagonal;
                    MinecraftClient.getInstance().setScreen((Screen) null);
                    BlockMeterClient.getConfigManager().save();
                }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 54,
                BUTTONWIDTH, 20,
                Text.translatable("blockmeter.showOthers", new Object[] {
                        Text.translatable(config.showOtherUsersBoxes ? "options.on" : "options.off")
                }), button -> {
                    System.err.println("IDK WHAT YOU ARE DOING");
                    config.showOtherUsersBoxes = !config.showOtherUsersBoxes;
                    MinecraftClient.getInstance().setScreen((Screen) null);
                    BlockMeterClient.getConfigManager().save();
                }));
    }

    @Override
    public void render(MatrixStack stack, final int int_1, final int int_2, final float float_1) {
        super.renderBackground(stack);
        super.render(stack, int_1, int_2, float_1);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}

class ColorButton extends ButtonWidget {
    Color color;
    int x;
    int y;
    int width;
    int height;
    boolean selected;
    boolean texture;
    MutableText text;

    ColorButton(final int x, final int y, final int width, final int height, final MutableText label,
            final float[] color,
            final boolean selected, boolean texture, PressAction onPress) {
        this(x, y, width, height, label, Color.ofRGB(color[0], color[1], color[2]), selected, texture, onPress);
    }

    @Override
    public void onPress() {
        System.out.println(color.getRed());
        System.out.println(color.getGreen());
        System.out.println(color.getBlue());
        System.err.println("IK WHAT YOU ARE DOING");
        super.onPress();
    }

    ColorButton(final int x, final int y, final int width, final int height, final MutableText label, final Color color,
            final boolean selected, boolean texture, PressAction onPress) {
        super(x, y, width, height, Text.literal(""), onPress);
        this.selected = false;
        this.color = color;
        this.x = x + 2;
        this.y = y + 2;
        this.width = width - 4;
        this.height = height - 4;
        this.setFocused(selected);
        this.selected = selected;
        this.text = label;
        this.texture = texture;
        // this.color = Color.ofRGBA(0.5f, 1f, 1f, 1f);
    }

    @Override
    public void render(MatrixStack stack, final int int_1, final int int_2, final float float_1) {
        super.render(stack, int_1, int_2, float_1);

        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int r = this.color.getRed();
        int g = this.color.getGreen();
        int b = this.color.getBlue();
        int a = texture ? 102 : 255;
        bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex((double) this.x - (texture ? 1 : 0), (double) this.y - (texture ? 1 : 0), 0.0)
                .color(r, g, b, a)
                .next();
        bufferBuilder.vertex((double) this.x - (texture ? 1 : 0), this.y + this.height + (texture ? 1 : 0), 0.0)
                .color(r, g, b, a)
                .next();
        bufferBuilder.vertex(this.x + this.width + (texture ? 1 : 0), this.y + this.height + (texture ? 1 : 0), 0.0)
                .color(r, g, b, a)
                .next();
        bufferBuilder.vertex(this.x + this.width + (texture ? 1 : 0), (double) this.y - (texture ? 1 : 0), 0.0)
                .color(r, g, b, a)
                .next();
        tessellator.draw();

        RenderSystem.enableTexture();

        if (text != null) {
            boolean dark = (0.299f * color.getRed() + 0.587f * color.getBlue() + 0.114f * color.getRed()) / 255f < 0.8f;

            @SuppressWarnings("resource")
            final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int text_width = textRenderer.getWidth(text);
            if (dark || texture)
                textRenderer.drawWithShadow(stack, text, x + width / 2 - text_width / 2, y + height / 2 - 4, 0xFFFFFF);
            else {
                // shadow
                textRenderer.draw(stack, text, x + width / 2 - text_width / 2 + 1, y + height / 2 - 3, 0xAAAAAA);
                textRenderer.draw(stack, text, x + width / 2 - text_width / 2, y + height / 2 - 4, 0);
            }
        }
    }
}
