package win.baruna.blockmeter.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.NarratorManager;
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
                        Color.ofOpaque(DyeColor.byId(colorIndex)
                                .getMapColor().color), config.colorIndex == colorIndex, false,
                        button -> {
                            ClientMeasureBox.setColorIndex(colorIndex);

                            final ClientMeasureBox currentBox = BlockMeterClient.getInstance().getCurrentBox();
                            if (currentBox != null)
                                currentBox.setColor(DyeColor.byId(colorIndex));
                            MinecraftClient.getInstance().setScreen(null);
                        }));
            }
        }

        this.addDrawableChild(new ButtonWidget.Builder(
                Text.translatable("blockmeter.keepColor", Text.translatable(config.incrementColor ? "options.off" :
                        "options.on")), button -> {
            config.incrementColor = !config.incrementColor;
            MinecraftClient.getInstance().setScreen(null);
            // Todo find a way to increment to a new Color if a box was created while
            // incrementColor was disabled
            BlockMeterClient.getConfigManager().save();
        })
                .position(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 10)
                .size(BUTTONWIDTH, 20)
                .build());

        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("blockmeter.diagonal",
                Text.translatable(config.innerDiagonal ? "options.on" : "options.off")), button -> {
            System.err.println("IDK WHAT YOU ARE DOING");
            config.innerDiagonal = !config.innerDiagonal;
            MinecraftClient.getInstance().setScreen(null);
            BlockMeterClient.getConfigManager().save();
        })
                .position(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 32)
                .size(BUTTONWIDTH, 20)
                .build());

        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("blockmeter.showOthers",
                Text.translatable(config.showOtherUsersBoxes ? "options.on" : "options.off")), button -> {
            System.err.println("IDK WHAT YOU ARE DOING");
            config.showOtherUsersBoxes = !config.showOtherUsersBoxes;
            MinecraftClient.getInstance().setScreen(null);
            BlockMeterClient.getConfigManager().save();
        })
                .position(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 54)
                .size(BUTTONWIDTH, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
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
        super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
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
    public void renderWidget(DrawContext context, final int int_1, final int int_2, final float float_1) {

        Tessellator tessellator = Tessellator.getInstance();
        var bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        int r = this.color.getRed();
        int g = this.color.getGreen();
        int b = this.color.getBlue();
        int a = texture ? 102 : 255;
        bufferBuilder.vertex(this.x - (texture ? 1 : 0), this.y - (texture ? 1 : 0), 0f)
                .color(r, g, b, a);
        bufferBuilder.vertex(this.x - (texture ? 1 : 0), this.y + this.height + (texture ? 1 : 0), 0f)
                .color(r, g, b, a);
        bufferBuilder.vertex(this.x + this.width + (texture ? 1 : 0), this.y + this.height + (texture ? 1 : 0), 0f)
                .color(r, g, b, a);
        bufferBuilder.vertex(this.x + this.width + (texture ? 1 : 0), this.y - (texture ? 1 : 0), 0f)
                .color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        if (text != null) {
            boolean dark = (0.299f * color.getRed() + 0.587f * color.getBlue() + 0.114f * color.getRed()) / 255f < 0.8f;

            final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int text_width = textRenderer.getWidth(text);
            if (dark || texture)
                context.drawText(textRenderer, text.asOrderedText(), x + width / 2 - text_width / 2,
                        y + height / 2 - 4, 0xFFFFFF, true);
            else {
                // shadow
                context.drawText(textRenderer, text, x + width / 2 - text_width / 2 + 1, y + height / 2 - 3, 0xAAAAAA
                        , false);
                context.drawText(textRenderer, text, x + width / 2 - text_width / 2, y + height / 2 - 4, 0, false);
            }
        }
    }
}
