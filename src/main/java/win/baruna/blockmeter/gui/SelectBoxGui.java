package win.baruna.blockmeter.gui;

import me.shedaniel.math.Color;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class SelectBoxGui extends Screen {

    private ClientMeasureBox[] boxes;
    private BlockPos block;

    public SelectBoxGui() {
        super(GameNarrator.NO_TITLE);
    }

    final static int BUTTONWIDTH = 250;
    final static int PADDING = 2;
    final static int BUTTONHEIGHT = 20;

    @Override
    protected void init() {
        final int uiHeight = (boxes.length + 1) * (BUTTONHEIGHT + PADDING);

        for (int i = 0; i < boxes.length; i++) {
            final ClientMeasureBox box = boxes[i];
            final var text = Component.translatable("blockmeter.boxToString",
                    box.getBlockStart().getX(), box.getBlockStart().getY(), box.getBlockStart().getZ(),
                    box.getBlockEnd().getX(), box.getBlockEnd().getY(), box.getBlockEnd().getZ());

            // text.setStyle(text.getStyle().withColor(TextColor.fromRgb(box.getColor().getSignColor())));

            this.addRenderableWidget(new ColorButton(this.width / 2 - (BUTTONWIDTH) / 2,
                    this.height / 2 - uiHeight / 2 + i * (BUTTONHEIGHT + PADDING), BUTTONWIDTH, BUTTONHEIGHT, text,
                    Color.ofOpaque(box.getColor()), false, true, button -> {
                BlockMeterClient.getInstance().editBox(box, block);
                Minecraft.getInstance().setScreen(null);
            }));

        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.cancel"),
                button -> Minecraft.getInstance().setScreen(null))
                .pos(this.width / 2 - BUTTONWIDTH / 2,
                        this.height / 2 - uiHeight / 2 + boxes.length * (BUTTONHEIGHT + PADDING) + PADDING)
                .size(BUTTONWIDTH, BUTTONHEIGHT)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setBoxes(ClientMeasureBox[] boxes) {
        this.boxes = boxes;
    }

    public void setBlock(BlockPos block) {
        this.block = block;
    }
}
