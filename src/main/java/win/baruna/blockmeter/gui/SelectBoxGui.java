package win.baruna.blockmeter.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class SelectBoxGui extends Screen {

    private ClientMeasureBox[] boxes;
    private BlockPos block;

    public SelectBoxGui() {
        super(NarratorManager.EMPTY);
    }

    final static int BUTTONWIDTH = 250;
    final static int PADDING = 2;
    final static int BUTTONHEIGHT = 20;

    @Override
    protected void init() {
        final int uiHeight = (boxes.length + 1) * (BUTTONHEIGHT + PADDING);

        for (int i = 0; i < boxes.length; i++) {
            final ClientMeasureBox box = boxes[i];
            final var text = Text.translatable("blockmeter.boxToString",
                    box.getBlockStart().getX(), box.getBlockStart().getY(), box.getBlockStart().getZ(),
                    box.getBlockEnd().getX(), box.getBlockEnd().getY(), box.getBlockEnd().getZ());

            // text.setStyle(text.getStyle().withColor(TextColor.fromRgb(box.getColor().getSignColor())));

            this.addDrawableChild(new ColorButton(this.width / 2 - (BUTTONWIDTH) / 2,
                    this.height / 2 - uiHeight / 2 + i * (BUTTONHEIGHT + PADDING), BUTTONWIDTH, BUTTONHEIGHT, text,
                    box.getColor().getColorComponents(), false, true, button -> {
                BlockMeterClient.getInstance().editBox(box, block);
                MinecraftClient.getInstance().setScreen(null);
            }));

        }

        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("gui.cancel"),
                button -> MinecraftClient.getInstance()
                .setScreen(null))
                .position(this.width / 2 - BUTTONWIDTH / 2,
                        this.height / 2 - uiHeight / 2 + boxes.length * (BUTTONHEIGHT + PADDING) + PADDING)
                .size(BUTTONWIDTH, BUTTONHEIGHT)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void setBoxes(ClientMeasureBox[] boxes) {
        this.boxes = boxes;
    }

    public void setBlock(BlockPos block) {
        this.block = block;
    }
}
