package win.baruna.blockmeter.gui;

import me.shedaniel.math.Color;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class OptionsGui extends Screen {

    public OptionsGui() {
        super(GameNarrator.NO_TITLE);
    }

    private final static int BUTTONWIDTH = 200;

    @Override
    protected void init() {
        ModConfig config = BlockMeterClient.getConfigManager().getConfig();

        // Create Color Selector
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                final int colorIndex = i * 4 + j;
                this.addRenderableWidget(new ColorButton(this.width / 2 - 44 + j * 22,
                        this.height / 2 - 88 + i * 22, 20, 20, null,
                        Color.ofOpaque(DyeColor.byId(colorIndex)
                                .getMapColor().col), config.colorIndex == colorIndex, false,
                        button -> {
                            ClientMeasureBox.setColorIndex(colorIndex);

                            final ClientMeasureBox currentBox = BlockMeterClient.getInstance().getCurrentBox();
                            if (currentBox != null)
                                currentBox.setColor(DyeColor.byId(colorIndex));
                            Minecraft.getInstance().setScreen(null);
                        }));
            }
        }

        this.addRenderableWidget(new Button.Builder(
                Component.translatable("blockmeter.keepColor", Component.translatable(config.incrementColor ? "options.off" :
                        "options.on")), button -> {
            config.incrementColor = !config.incrementColor;
            Minecraft.getInstance().setScreen(null);
            // Todo find a way to increment to a new Color if a box was created while
            // incrementColor was disabled
            BlockMeterClient.getConfigManager().save();
        })
                .pos(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 10)
                .size(BUTTONWIDTH, 20)
                .build());

        this.addRenderableWidget(new Button.Builder(Component.translatable("blockmeter.diagonal",
                Component.translatable(config.innerDiagonal ? "options.on" : "options.off")), button -> {
            System.err.println("IDK WHAT YOU ARE DOING");
            config.innerDiagonal = !config.innerDiagonal;
            Minecraft.getInstance().setScreen(null);
            BlockMeterClient.getConfigManager().save();
        })
                .pos(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 32)
                .size(BUTTONWIDTH, 20)
                .build());

        this.addRenderableWidget(new Button.Builder(Component.translatable("blockmeter.showOthers",
                Component.translatable(config.showOtherUsersBoxes ? "options.on" : "options.off")), button -> {
            System.err.println("IDK WHAT YOU ARE DOING");
            config.showOtherUsersBoxes = !config.showOtherUsersBoxes;
            Minecraft.getInstance().setScreen(null);
            BlockMeterClient.getConfigManager().save();
        })
                .pos(this.width / 2 - BUTTONWIDTH / 2, this.height / 2 + 54)
                .size(BUTTONWIDTH, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}

class ColorButton extends Button {
    Color color;
    int x;
    int y;
    int width;
    int height;
    boolean selected;
    boolean texture;
    MutableComponent text;
    private static final WidgetSprites TEXTURES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted")
    );

    @Override
    public void onPress(InputWithModifiers input) {
        System.out.println(color.getRed());
        System.out.println(color.getGreen());
        System.out.println(color.getBlue());
        System.err.println("IK WHAT YOU ARE DOING");
        super.onPress(input);
    }

    ColorButton(final int x, final int y, final int width, final int height, final MutableComponent label, final Color color,
                final boolean selected, boolean texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
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
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                TEXTURES.get(this.active, this.isHoveredOrFocused()),
                x, y, width, height, color.getColor()
        );
        if (text != null) {
            boolean dark = (0.299f * color.getRed() + 0.587f * color.getBlue() + 0.114f * color.getRed()) / 255f < 0.8f;

            final Font textRenderer = Minecraft.getInstance().font;
            int text_width = textRenderer.width(text);
            if (dark || texture)
                context.text(textRenderer, text.getVisualOrderText(), x + width / 2 - text_width / 2,
                        y + height / 2 - 4, 0xFFFFFF, true);
            else {
                // shadow
                context.text(textRenderer, text, x + width / 2 - text_width / 2 + 1, y + height / 2 - 3, 0xAAAAAA,
                        false);
                context.text(textRenderer, text, x + width / 2 - text_width / 2, y + height / 2 - 4, 0, false);
            }
        }
    }
}
