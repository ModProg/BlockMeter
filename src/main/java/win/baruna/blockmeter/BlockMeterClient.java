package win.baruna.blockmeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.gui.OptionsGui;

public class BlockMeterClient implements ClientModInitializer {
    public static BlockMeterClient instance;

    /**
     * The current state of the BlockMeter (activated/deactivated)
     */
    private boolean active;

    /**
     * The Item selected as BlockMeter
     */
    private Item currentItem;

    /**
     * The List of Measuring-Boxes currently created by the current User
     */
    private List<ClientMeasureBox> boxes;

    /**
     * A Map of Lists of Boxes currently created by other Users, with Text being the
     * Username
     */
    private Map<Text, List<ClientMeasureBox>> otherUsersBoxes;

    /**
     * The Menu for changing of Color etc.
     */
    private OptionsGui menu;

    /**
     * Setting to enable other Users Measuring-Boxes
     */
    private boolean showOtherUsersBoxes;

    public BlockMeterClient() {
        active = false;
        boxes = new ArrayList<>();
        menu = new OptionsGui();
        otherUsersBoxes = null;
        showOtherUsersBoxes = false;
        BlockMeterClient.instance = this;
    }

    /**
     * Clears the created Boxes and disables BlockMeter
     */
    public void clear() {
        active = false;
        boxes.clear();
        ClientMeasureBox.colorIndex = 0;
    }

    /**
     * Gets Triggered when the Player disconnects from the Server
     */
    public void onDisconnected() {
        otherUsersBoxes = null;
        clear();
    }

    /**
     * Gets Triggered when the Player connects to the Server
     */
    public void onConnected() {
        sendBoxList(); // to make the server send other user's boxes
    }

    @Override
    public void onInitializeClient() {
        final KeyBinding keyBinding = new KeyBinding("key.blockmeter.assign", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.blockmeter.key");
        final KeyBinding keyBindingMenu = new KeyBinding("key.blockmeter.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.blockmeter.key");
        KeyBindingHelper.registerKeyBinding(keyBinding);
        KeyBindingHelper.registerKeyBinding(keyBindingMenu);
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        ClientSidePacketRegistry.INSTANCE.register(BlockMeter.S2CPacketIdentifier, this::receiveBoxList);
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            if (keyBinding.wasPressed()) {
                if (this.active) {
                    if (Screen.hasShiftDown()) {
                        if (this.boxes.size() > 0) {
                            this.boxes.remove(this.boxes.size() - 1);
                            sendBoxList();
                        }
                        e.player.sendMessage(new TranslatableText("blockmeter.clearlast"), true);
                    } else {
                        this.active = false;
                        e.player.sendMessage(new TranslatableText("blockmeter.toggle.off", new Object[0]), true);
                        this.boxes.clear();
                        sendBoxList();
                    }
                } else {
                    active = true;
                    ItemStack itemStack = e.player.getMainHandStack();
                    currentItem = itemStack.getItem();
                    e.player.sendMessage(new TranslatableText("blockmeter.toggle.on", new Object[] {
                            new TranslatableText(itemStack.getTranslationKey(), new Object[0])
                    }), true);
                }
            }

            if (keyBindingMenu.wasPressed()
                    && active
                    && MinecraftClient.getInstance().player.getMainHandStack().getItem() == this.currentItem) {
                MinecraftClient.getInstance().openScreen((Screen) this.menu);
            }

            if (this.active && this.boxes.size() > 0) {
                final ClientMeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);
                if (!lastBox.isFinished()) {
                    lastBox.color = DyeColor.byId(ClientMeasureBox.colorIndex);
                    final HitResult rayHit = e.player.rayTrace((double) e.interactionManager.getReachDistance(), 1.0f, false);
                    if (rayHit.getType() == HitResult.Type.BLOCK) {
                        final BlockHitResult blockHitResult = (BlockHitResult) rayHit;
                        lastBox.setBlockEnd(new BlockPos(blockHitResult.getBlockPos()));
                    }
                }
            }
        });

        UseBlockCallback.EVENT.register(
                (playerEntity, world, hand, hitResult) -> this.addBox(playerEntity, hitResult));
    }

    /**
     * Handles the right click Event for creating and confirming new Measuring-Boxes
     * 
     * @param playerEntity
     *            the player object
     * @param hitResult
     * @return PASS if not active or wrong item, FAIL when successful, to not send
     *         the event to the server
     */
    private ActionResult addBox(final PlayerEntity playerEntity, final BlockHitResult hitResult) {
        if (!this.active) {
            return ActionResult.PASS;
        }
        if (playerEntity.getMainHandStack().getItem().equals(this.currentItem)) {
            final BlockPos block = hitResult.getBlockPos();

            if (this.boxes.size() > 0) {
                final ClientMeasureBox lastBox = this.boxes.get(this.boxes.size() - 1);

                if (lastBox.isFinished()) {
                    final ClientMeasureBox box = new ClientMeasureBox(block, playerEntity.world.getDimension().getSuffix());
                    this.boxes.add(box);
                } else {
                    lastBox.setBlockEnd(block);
                    lastBox.setFinished();
                    sendBoxList();
                }
            } else {
                final ClientMeasureBox box2 = new ClientMeasureBox(block, playerEntity.world.getDimension().getSuffix());
                this.boxes.add(box2);

            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private void sendBoxList() {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(boxes.size());
        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).writePacketBuf(passedData);
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(BlockMeter.C2SPacketIdentifier, passedData);
    }

    private void receiveBoxList(PacketContext context, PacketByteBuf data) {
        Map<Text, List<ClientMeasureBox>> receivedBoxes = new HashMap<>();
        int playerCount = data.readInt();
        for (int i = 0; i < playerCount; i++) {
            Text playerName = data.readText();
            int boxCount = data.readInt();
            List<ClientMeasureBox> boxes = new ArrayList<ClientMeasureBox>(boxCount);
            for (int j = 0; j < boxCount; j++) {
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
        final String currentDimension = client.player.world.getDimension().getSuffix();
        client.textRenderer.draw(stack, "XXX", -100, -100, 0); // MEH! but this seems to be needed to get the first background rectangle
        if (showOtherUsersBoxes && otherUsersBoxes != null && otherUsersBoxes.size() > 0) {
            this.otherUsersBoxes.forEach((playerText, boxList) -> {
                boxList.forEach(box -> box.render(camera, stack, currentDimension, playerText));
            });
            this.boxes.forEach(box -> {
                if (!box.isFinished())
                    box.render(camera, stack, currentDimension);
            });
        } else if (this.active) {
            this.boxes.forEach(box -> box.render(camera, stack, currentDimension));
        }
    }

    public static boolean getShowOtherUsers() {
        return instance.showOtherUsersBoxes;
    }

    public static void setShowOtherUsers(boolean b) {
        instance.showOtherUsersBoxes = b;
        instance.sendBoxList(); // to trigger a server response to get other people's boxes
    }
}
