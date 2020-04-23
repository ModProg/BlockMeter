package win.baruna.blockmeter;  

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import win.baruna.blockmeter.gui.OptionsGui;

public class BlockMeter implements ClientModInitializer
{
    public static BlockMeter instance;
    private boolean active;
    private Item currentItem;
    private List<MeasureBox> boxes;
    private OptionsGui menu;

    public BlockMeter() {
        this.active = false;
        this.boxes = new ArrayList<>();
        this.menu = new OptionsGui();
        BlockMeter.instance = this;
    }
    public void clear() {
        this.boxes.clear();
        MeasureBox.colorIndex = 0;
        this.active = false;

    }
    
    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.addCategory("blockmeter.key");

        final FabricKeyBinding keyBinding = FabricKeyBinding.Builder
                .create(new Identifier("blockmeter:assign"), InputUtil.Type.KEYSYM, 77, "blockmeter.key").build();
        final FabricKeyBinding keyBindingMenu = FabricKeyBinding.Builder
                .create(new Identifier("blockmeter:menu"), InputUtil.Type.KEYSYM, 342, "blockmeter.key").build();

        KeyBindingRegistry.INSTANCE.register(keyBinding);
        KeyBindingRegistry.INSTANCE.register(keyBindingMenu);

        ClientTickCallback.EVENT.register(e -> {

            if (keyBinding.wasPressed()) {
                if (this.active) {
                    if (Screen.hasShiftDown()) {
                        if (this.boxes.size() > 0) {
                            this.boxes.remove(this.boxes.size()-1);
                        }
                        e.player.sendMessage(new TranslatableText("blockmeter.clearlast"), true);
                    } else {
                        this.active = false;
                        e.player.sendMessage(new TranslatableText("blockmeter.toggle.off", new Object[0]), true);
                        this.boxes.clear();
                    }
                } else {
                    active = true;
                    ItemStack itemStack = e.player.getMainHandStack();
                    currentItem = itemStack.getItem();
                    e.player.sendMessage(new TranslatableText("blockmeter.toggle.on", new Object[] { new TranslatableText(itemStack.getTranslationKey(), new Object[0]) }), true);
                }              
            }

            if (keyBindingMenu.wasPressed()
            &&  active
            &&  MinecraftClient.getInstance().player.getMainHandStack().getItem() == this.currentItem) {
                MinecraftClient.getInstance().openScreen((Screen)this.menu);
            }

            if (this.active && this.boxes.size() > 0) {
                final MeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);
                if (!lastBox.isFinished()) {
                    final HitResult rayHit = e.player.rayTrace((double)e.interactionManager.getReachDistance(), 1.0f, false);
                    if (rayHit.getType() == HitResult.Type.BLOCK) {
                        final BlockHitResult blockHitResult = (BlockHitResult)rayHit;
                        lastBox.setBlockEnd(new BlockPos(blockHitResult.getBlockPos()));
                    }                  }
            }
        });
        
        UseBlockCallback.EVENT.register(
            (playerEntity, world, hand, hitResult)
            -> 
            this.addBox(playerEntity, hitResult));
    }



    private ActionResult addBox(final PlayerEntity playerEntity, final BlockHitResult hitResult) {
        if (!this.active) {
            return ActionResult.PASS;          }
        if (playerEntity.getMainHandStack().getItem().equals(this.currentItem)) {
            final BlockPos block = hitResult.getBlockPos();

            if (this.boxes.size() > 0) {
                final MeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);

                if (lastBox.isFinished()) {
                    final MeasureBox box = new MeasureBox(block, playerEntity.dimension);
                    this.boxes.add(box);
                }                  else {
                    lastBox.setBlockEnd(block);
                    lastBox.setFinished();
                }              }
            else {
                final MeasureBox box2 = new MeasureBox(block, playerEntity.dimension);
                this.boxes.add(box2);

            }
            return ActionResult.FAIL;

        }
        return ActionResult.PASS;
    }
    
    public void renderOverlay(float partialTicks, MatrixStack stack) {
        if (!this.active) {
            return;         
        }
        final MinecraftClient instance = MinecraftClient.getInstance();
        final Camera camera = instance.gameRenderer.getCamera();
        final DimensionType currentDimension = instance.player.dimension;
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(stack.peek().getModel());
        this.boxes.forEach(box -> box.render(camera, currentDimension));
        RenderSystem.popMatrix();
    }
}
