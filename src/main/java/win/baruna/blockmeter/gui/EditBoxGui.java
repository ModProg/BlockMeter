package win.baruna.blockmeter.gui;

import win.baruna.blockmeter.measurebox.ClientMeasureBox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static win.baruna.blockmeter.gui.SelectBoxGui.*;

public class EditBoxGui extends Screen {

    private ClientMeasureBox box;
    private BlockPos block;

    public EditBoxGui() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    protected void init() {
        final int uiHeight = 2 * (BUTTONHEIGHT + PADDING);
        AtomicInteger buttonIdx = new AtomicInteger(1);

        Consumer<Button.Builder> addButton = button -> this.addRenderableWidget(button
                .pos(this.width / 2 - BUTTONWIDTH / 2,
                        this.height / 2 - uiHeight / 2 + buttonIdx.getAndIncrement() * (BUTTONHEIGHT + PADDING) + PADDING)
                .size(BUTTONWIDTH, BUTTONHEIGHT)
                .build());

        addButton.accept(new Button.Builder(Component.translatable("blockmeter.moveCorner"),
                button -> {
                    box.loosenCorner(block);
                    Minecraft.getInstance().setScreen(null);
                }));

        addButton.accept(new Button.Builder(Component.translatable("blockmeter.restrictMining", Component.translatable(box.miningRestriction.translation)), button -> {
            box.miningRestriction = box.miningRestriction.next();
            Minecraft.getInstance().setScreen(null);
        }));

        addButton.accept(new Button.Builder(Component.translatable("gui.cancel"),
                button -> Minecraft.getInstance().setScreen(null)));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setBox(ClientMeasureBox box) {
        this.box = box;
    }

    public void setBlock(BlockPos block) {
        this.block = block;
    }
}
