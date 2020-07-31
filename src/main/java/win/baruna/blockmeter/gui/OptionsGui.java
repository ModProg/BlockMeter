package win.baruna.blockmeter.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ClientMeasureBox;
import win.baruna.blockmeter.ModConfig;

public class OptionsGui extends Screen {

    public OptionsGui() {
        super(NarratorManager.EMPTY);
    }

    final static int BUTTONWIDTH = 200;

    @Override
    protected void init() {
        ModConfig config = BlockMeterClient.confmgr.getConfig();
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                this.addButton((AbstractButtonWidget) new ColorSelectButton(this.width / 2 - 44 + j * 22, this.height / 2 - 88 + i * 22, 20, 20, "", i * 4 + j));
            }
        }
        this.addButton((AbstractButtonWidget) new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 10, BUTTONWIDTH, 20,
                new TranslatableText("blockmeter.keepcolor", new Object[] {
                        new TranslatableText(config.incrementColor ? "options.off" : "options.on")
                }), button -> {
                    config.incrementColor = !config.incrementColor;
                    MinecraftClient.getInstance().openScreen((Screen) null);
                    // Todo find a way to increment to a new Color if a box was created while
                    // incrementColor was disabled
                    BlockMeterClient.confmgr.save();
                }));
        this.addButton((AbstractButtonWidget) new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 32, BUTTONWIDTH, 20,
                new TranslatableText("blockmeter.diagonal", new Object[] {
                        new TranslatableText(config.innerDiagonal ? "options.on" : "options.off")
                }), button -> {
                    config.innerDiagonal = !config.innerDiagonal;
                    MinecraftClient.getInstance().openScreen((Screen) null);
                    BlockMeterClient.confmgr.save();
                }));
        this.addButton((AbstractButtonWidget) new ButtonWidget(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 54, BUTTONWIDTH, 20,
                new TranslatableText("blockmeter.showothers", new Object[] {
                        new TranslatableText(config.showOtherUsersBoxes ? "options.on" : "options.off")
                }), button -> {
                    config.showOtherUsersBoxes = !config.showOtherUsersBoxes;
                    MinecraftClient.getInstance().openScreen((Screen) null);
                    BlockMeterClient.confmgr.save();
                }));
    }

    @Override
    public void render(MatrixStack stack, final int int_1, final int int_2, final float float_1) {
        super.renderBackground(stack);
        super.render(stack, int_1, int_2, float_1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class ColorSelectButton extends ButtonWidget {
        float[] color;
        int x;
        int y;
        int width;
        int height;
        boolean selected;

        ColorSelectButton(final int x, final int y, final int width, final int height, final String string, final int colorIndex) {
            super(x, y, width, height, new LiteralText(string), button -> {
                ClientMeasureBox.selectColorIndex(colorIndex);

                final ClientMeasureBox currentBox = BlockMeterClient.instance.currentBox();
                if (currentBox != null)
                    currentBox.setColor(DyeColor.byId(colorIndex));
                MinecraftClient.getInstance().openScreen((Screen) null);
            });
            this.selected = false;
            this.color = DyeColor.byId(colorIndex).getColorComponents();
            this.x = x + 2;
            this.y = y + 2;
            this.width = width - 4;
            this.height = height - 4;
            if (BlockMeterClient.confmgr.getConfig().colorIndex == colorIndex) {
                this.setFocused(true);
                this.selected = true;
            }
        }

        @Override
        public void render(MatrixStack stack, final int int_1, final int int_2, final float float_1) {
            super.render(stack, int_1, int_2, float_1);

            GlStateManager.disableTexture();
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferBuilder = tessellator.getBuffer();
            GlStateManager.color4f(this.color[0], this.color[1], this.color[2], 1.0f);
            bufferBuilder.begin(7, VertexFormats.POSITION);
            bufferBuilder.vertex((double) this.x, (double) this.y, 0.0).next();
            bufferBuilder.vertex((double) this.x, this.y + this.height, 0.0).next();
            bufferBuilder.vertex(this.x + this.width, this.y + this.height, 0.0).next();
            bufferBuilder.vertex(this.x + this.width, (double) this.y, 0.0).next();
            tessellator.draw();

            if (this.selected) {
                final float[] highlightColor = DyeColor.YELLOW.getColorComponents();
                GlStateManager.color4f(highlightColor[0], highlightColor[1], highlightColor[2], 1.0f);
                GlStateManager.lineWidth(2.0f);
                bufferBuilder.begin(3, VertexFormats.POSITION);
                bufferBuilder.vertex(this.x - 2.0, this.y - 2.0, 0.0).next();
                bufferBuilder.vertex(this.x - 2.0, this.y - 2.0 + this.height + 4.0, 0.0).next();
                bufferBuilder.vertex(this.x - 2.0 + this.width + 4.0, this.y - 2.0 + this.height + 4.0, 0.0).next();
                bufferBuilder.vertex(this.x - 2.0 + this.width + 4.0, this.y - 2.0, 0.0).next();
                bufferBuilder.vertex(this.x - 2.0, this.y - 2.0, 0.0).next();
                tessellator.draw();

            }
            GlStateManager.enableTexture();
        }
    }
}
