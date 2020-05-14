package win.baruna.blockmeter;  

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import win.baruna.blockmeter.gui.OptionsGui;

public class BlockMeterClient implements ClientModInitializer
{
    public static BlockMeterClient instance;
    private boolean active;
    private Item currentItem;
    private List<ClientMeasureBox> boxes;
    private Map<Text, List<ClientMeasureBox>> otherUsersBoxes;
    private OptionsGui menu;
    private boolean showOtherUsersBoxes;

    public BlockMeterClient() {
        active = false;
        boxes = new ArrayList<>();
        menu = new OptionsGui();
        otherUsersBoxes = null;
        showOtherUsersBoxes = false;
        BlockMeterClient.instance = this;
    }

    public void clear() {
        active = false;
        boxes.clear();
        ClientMeasureBox.colorIndex = 0;
    }
    
    public void disconnected() {
        otherUsersBoxes = null;
        clear();
    }
    
    public void connected() {
        sendBoxList();              // to make the server send other user's boxes
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
        
        ClientSidePacketRegistry.INSTANCE.register(BlockMeter.S2CPacketIdentifier, this::receiveBoxList);
        ClientTickCallback.EVENT.register(e -> {

            if (keyBinding.wasPressed()) {
                if (this.active) {
                    if (Screen.hasShiftDown()) {
                        if (this.boxes.size() > 0) {
                            this.boxes.remove(this.boxes.size()-1);
                            sendBoxList();
                        }
                        e.player.addChatMessage(new TranslatableText("blockmeter.clearlast"), true);
                    } else {
                        this.active = false;
                        e.player.addChatMessage(new TranslatableText("blockmeter.toggle.off", new Object[0]), true);
                        this.boxes.clear();
                        sendBoxList();
                    }
                } else {
                    active = true;
                    ItemStack itemStack = e.player.getMainHandStack();
                    currentItem = itemStack.getItem();
                    e.player.addChatMessage(new TranslatableText("blockmeter.toggle.on", new Object[] { new TranslatableText(itemStack.getTranslationKey(), new Object[0]) }), true);
                }              
            }

            if (keyBindingMenu.wasPressed()
            &&  active
            &&  MinecraftClient.getInstance().player.getMainHandStack().getItem() == this.currentItem) {
                MinecraftClient.getInstance().openScreen((Screen)this.menu);
            }

            if (this.active && this.boxes.size() > 0) {
                final ClientMeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);
                if (!lastBox.isFinished()) {
                    lastBox.color = DyeColor.byId(ClientMeasureBox.colorIndex);
                    final HitResult rayHit = e.player.rayTrace((double)e.interactionManager.getReachDistance(), 1.0f, false);
                    if (rayHit.getType() == HitResult.Type.BLOCK) {
                        final BlockHitResult blockHitResult = (BlockHitResult)rayHit;
                        lastBox.setBlockEnd(new BlockPos(blockHitResult.getBlockPos()));
                    }
                }
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
                final ClientMeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);

                if (lastBox.isFinished()) {
                    final ClientMeasureBox box = new ClientMeasureBox(block, playerEntity.dimension);
                    this.boxes.add(box);
                } else {
                    lastBox.setBlockEnd(block);
                    lastBox.setFinished();
                    sendBoxList();
                }
            } else {
                final ClientMeasureBox box2 = new ClientMeasureBox(block, playerEntity.dimension);
                this.boxes.add(box2);

            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
    
    private void sendBoxList() {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(boxes.size());
        for (int i=0; i<boxes.size(); i++) {
            boxes.get(i).writePacketBuf(passedData);
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(BlockMeter.C2SPacketIdentifier, passedData);
    }
    
    private void receiveBoxList(PacketContext context, PacketByteBuf data) {
        Map<Text, List<ClientMeasureBox>> receivedBoxes = new HashMap<>();
        int playerCount = data.readInt();
        for (int i=0; i<playerCount; i++) {
            Text playerName = data.readText();
            int boxCount = data.readInt();
            List<ClientMeasureBox> boxes = new ArrayList(boxCount);
            for (int j=0; j<boxCount; j++) {
                boxes.add(ClientMeasureBox.fromPacketByteBuf(data));
            }
            receivedBoxes.put(playerName, boxes);
        }
        context.getTaskQueue().execute(() -> {
            otherUsersBoxes = receivedBoxes;
        });
    }
    
    public void renderOverlay(float partialTicks, MatrixStack stack) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Camera camera = client.gameRenderer.getCamera();
        final DimensionType currentDimension = client.player.dimension;
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(stack.peek().getModel());
        if (showOtherUsersBoxes && otherUsersBoxes != null && otherUsersBoxes.size()>0) {
            this.otherUsersBoxes.forEach((playerText, boxList) -> {
                boxList.forEach(box -> box.render(camera, currentDimension, playerText));
            });
            this.boxes.forEach(box -> {
                if (!box.isFinished()) 
                    box.render(camera, currentDimension);
            });
        }
        else if (this.active) {
            this.boxes.forEach(box -> box.render(camera, currentDimension));
        }
        RenderSystem.popMatrix();
    }
    
    public static boolean getShowOtherUsers() { return instance.showOtherUsersBoxes; }
    public static void setShowOtherUsers(boolean b) { 
        instance.showOtherUsersBoxes = b;
        instance.sendBoxList();         // to trigger a server response to get other people's boxes
    }
}
