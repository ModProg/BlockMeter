package win.baruna.blockmeter.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static win.baruna.blockmeter.gui.SelectBoxGui.*;

public class EditBoxGui extends Screen {

    private ClientMeasureBox box;
    private BlockPos block;

    public EditBoxGui() {
        super(NarratorManager.EMPTY);
    }

    @Override
    protected void init() {
        final int uiHeight = 2 * (BUTTONHEIGHT + PADDING);
        AtomicInteger buttonIdx = new AtomicInteger(1);

        Consumer<ButtonWidget.Builder> addButton = button -> this.addDrawableChild(button
                .position(this.width / 2 - BUTTONWIDTH / 2,
                        this.height / 2 - uiHeight / 2 + buttonIdx.getAndIncrement() * (BUTTONHEIGHT + PADDING) + PADDING)
                .size(BUTTONWIDTH, BUTTONHEIGHT)
                .build());

        addButton.accept(new ButtonWidget.Builder(Text.translatable("blockmeter.moveCorner"),
                button -> {
                    box.loosenCorner(block);
                    MinecraftClient.getInstance().setScreen(null);
                }));

        addButton.accept(new ButtonWidget.Builder(Text.translatable("blockmeter.restrictMining", Text.translatable(box.miningRestriction.translation)), button -> {
            box.miningRestriction = box.miningRestriction.next();
            MinecraftClient.getInstance().setScreen(null);
        }));

        addButton.accept(new ButtonWidget.Builder(Text.translatable("gui.cancel"),
                button -> MinecraftClient.getInstance().setScreen(null)));
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

    public void setBox(ClientMeasureBox box) {
        this.box = box;
    }

    public void setBlock(BlockPos block) {
        this.block = block;
    }
}
