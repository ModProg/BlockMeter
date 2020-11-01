package win.baruna.blockmeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.gui.OptionsGui;
import win.baruna.blockmeter.gui.SelectBoxGui;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class BlockMeterClient implements ClientModInitializer {
    /**
     * Currently running Instance of BlockMeterClient
     */
    private static BlockMeterClient instance;

    /**
     * Accessor for the BlockMeterClient Instance
     * 
     * @return running Instance of BlockMeterClient
     */
    public static BlockMeterClient getInstance() {
        return instance;
    }

    /**
     * ConfigManager of BlockMeter
     */
    private static ConfigManager<ModConfig> confMgr;

    /**
     * Accessor for the ModConfigManager
     * 
     * @return ConfigManager for handling the Config
     */
    public static ConfigManager<ModConfig> getConfigManager() {
        return confMgr;
    }

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
     * The QuickMenu for changing of Color etc.
     */
    private OptionsGui quickMenu;


    /**
     * The QuickMenu for selecting on of multiple Boxes.
     */
    private SelectBoxGui selectBoxGui;

    public BlockMeterClient() {
        active = false;
        boxes = new ArrayList<>();
        quickMenu = new OptionsGui();
        selectBoxGui = new SelectBoxGui();
        otherUsersBoxes = null;
        BlockMeterClient.instance = this;
    }

    /**
     * Disables BlockMeter
     */
    public void disable() {
        active = false;
        if (confMgr.getConfig().deleteBoxesOnDisable) {
            clear();
        }
    }

    /**
     * Resets Blockmeter to be used in an other World
     */
    public void reset() {
        otherUsersBoxes = null;
        boxes.clear();

        // Resets Color to always start with white in an other world
        ModConfig cfg = confMgr.getConfig();
        if (cfg.incrementColor) {
            cfg.colorIndex = 0;
            confMgr.save();
        }
    }

    /**
     * Clears Boxes and sends this information to the server
     */
    public boolean clear() {
        boolean hasBox = boxes.size() > 0;
        boxes.clear();
        sendBoxList();

        // Reset the color as all Boxes where deleted
        ModConfig cfg = confMgr.getConfig();
        if (cfg.incrementColor) {
            cfg.colorIndex = 0;
            confMgr.save();
        }
        return hasBox;
    }

    /**
     * Removes the last box
     */
    public boolean undo() {
        if (this.boxes.size() == 0)
            return false;

        this.boxes.remove(this.boxes.size() - 1);
        sendBoxList();

        ModConfig cfg = confMgr.getConfig();
        if (cfg.incrementColor) {
            cfg.colorIndex = Math.floorMod(cfg.colorIndex - 1, DyeColor.values().length);
            confMgr.save();
        }

        return true;
    }

    public void renderOverlay(float partialTicks, MatrixStack stack) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Camera camera = client.gameRenderer.getCamera();
        final Identifier currentDimension = client.player.world.getRegistryKey().getValue();

        final ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        // MEH! but this seems to be needed to get the first background
        // rectangle
        client.textRenderer.draw(stack, "XXX", -100, -100, 0);

        if (this.active || cfg.showBoxesWhenDisabled)
            if (cfg.showOtherUsersBoxes) {
                if (otherUsersBoxes != null && otherUsersBoxes.size() > 0) {
                    this.otherUsersBoxes.forEach((playerText, boxList) -> {
                        boxList.forEach(box -> box.render(camera, stack, currentDimension, playerText));
                    });
                    this.boxes.forEach(box -> {
                        if (!box.isFinished())
                            box.render(camera, stack, currentDimension);
                    });
                }
                if (!cfg.sendBoxes)
                    this.boxes.forEach(box -> {
                        if (box.isFinished())
                            box.render(camera, stack, currentDimension, client.player.getDisplayName());
                        else
                            box.render(camera, stack, currentDimension);
                    });
            } else
                this.boxes.forEach(box -> box.render(camera, stack, currentDimension));

    }

    /**
     * Gets Triggered when the Player disconnects from the Server
     */
    public void onDisconnected() {
        reset();
    }

    /**
     * Gets Triggered when the Player connects to the Server
     */
    public void onConnected() {
        sendBoxList(); // to make the server send other user's boxes
    }

    /**
     * Returns the currently active box
     * 
     * @return currently open box or null if none
     */
    public ClientMeasureBox getCurrentBox() {
        return boxes.stream().filter(box -> !box.isFinished()).findAny().orElse(null);
    }

    @Override
    public void onInitializeClient() {
        final KeyBinding keyBinding = new KeyBinding("key.blockmeter.assign", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M,
                "category.blockmeter.key");
        final KeyBinding keyBindingMenu = new KeyBinding("key.blockmeter.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT, "category.blockmeter.key");
        KeyBindingHelper.registerKeyBinding(keyBinding);
        KeyBindingHelper.registerKeyBinding(keyBindingMenu);

        // This is ugly I know, but I did not find something better
        // (Issue in AutoConfig https://github.com/shedaniel/AutoConfig/issues/13)
        confMgr = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ClientSidePacketRegistry.INSTANCE.register(BlockMeter.S2CPacketIdentifier, this::handleServerBoxList);
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            if (keyBinding.wasPressed()) {
                if (Screen.hasShiftDown()) {
                    if (undo())
                        e.player.sendMessage(
                                new TranslatableText("blockmeter.clearLast"),
                                true);
                } else if (Screen.hasControlDown()) {
                    if (clear())
                        e.player.sendMessage(
                                new TranslatableText("blockmeter.clearAll"),
                                true);
                } else if (this.active) {
                    disable();
                    e.player.sendMessage(new TranslatableText("blockmeter.toggle.off", new Object[0]), true);
                } else {
                    active = true;
                    ItemStack itemStack = e.player.getMainHandStack();
                    currentItem = itemStack.getItem();
                    e.player.sendMessage(
                            new TranslatableText("blockmeter.toggle.on",
                                    new Object[] {
                                            new TranslatableText(itemStack.getTranslationKey(), new Object[0]) }),
                            true);
                }
            }

            if (keyBindingMenu.wasPressed() && active
                    && MinecraftClient.getInstance().player.getMainHandStack().getItem() == this.currentItem) {
                MinecraftClient.getInstance().openScreen((Screen) this.quickMenu);
            }

            // Updates Selection preview
            if (this.active && this.boxes.size() > 0) {
                final ClientMeasureBox currentBox = getCurrentBox();
                if (currentBox != null) {
                    final HitResult rayHit = e.player.rayTrace((double) e.interactionManager.getReachDistance(), 1.0f,
                            false);
                    if (rayHit.getType() == HitResult.Type.BLOCK) {
                        final BlockHitResult blockHitResult = (BlockHitResult) rayHit;
                        currentBox.setBlockEnd(new BlockPos(blockHitResult.getBlockPos()));
                    }
                }
            }
        });

        UseBlockCallback.EVENT
                .register((playerEntity, world, hand, hitResult) -> this.onBlockMeterClick(playerEntity, hitResult));
    }

    /**
     * Handles the right click Event for creating and confirming new Measuring-Boxes
     * 
     * @param playerEntity the player object
     * @param hitResult
     * @return PASS if not active or wrong item, FAIL when successful, to not send
     *         the event to the server
     */
    private ActionResult onBlockMeterClick(final PlayerEntity playerEntity, final BlockHitResult hitResult) {
        if (!this.active) {
            return ActionResult.PASS;
        }
        if (playerEntity.getMainHandStack().getItem().equals(this.currentItem)) {
            final BlockPos block = hitResult.getBlockPos();

            ClientMeasureBox currentBox = getCurrentBox();

            if (currentBox == null) {
                if (Screen.hasShiftDown()) {
                    ClientMeasureBox[] boxes = findBoxes(block);
                    switch (boxes.length) {
                    case 0:
                        break;
                    case 1:
                        boxes[0].loosenCorner(block);
                        break;
                    default:
                        this.selectBoxGui.setBoxes(boxes);
                        this.selectBoxGui.setBlock(block);
                        MinecraftClient.getInstance().openScreen((Screen) this.selectBoxGui);
                        break;
                    }
                } else {
                    final ClientMeasureBox box = ClientMeasureBox.getBox(block,
                            playerEntity.world.getRegistryKey().getValue());
                    this.boxes.add(box);
                }
            } else {
                currentBox.setBlockEnd(block);
                currentBox.setFinished();
                sendBoxList();
            }

            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    /**
     * Finds a box to be edited when selecting this block
     * 
     * @param block selected block
     * @return Box to be edited
     */
    private ClientMeasureBox[] findBoxes(BlockPos block) {
        return boxes.stream().filter(box -> box.isCorner(block)).toArray(ClientMeasureBox[]::new);
    }

    /**
     * Sends BoxList to Server if enabled in the config
     */
    private void sendBoxList() {
        if (!AutoConfig.getConfigHolder(ModConfig.class).getConfig().sendBoxes)
            return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(boxes.size());
        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).writePacketBuf(passedData);
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(BlockMeter.C2SPacketIdentifier, passedData);
    }

    /**
     * handles the BoxList of other Players
     * 
     * @param context
     * @param data
     */
    private void handleServerBoxList(PacketContext context, PacketByteBuf data) {
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

}
