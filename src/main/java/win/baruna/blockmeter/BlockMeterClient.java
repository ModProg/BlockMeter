package win.baruna.blockmeter;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;
import win.baruna.blockmeter.gui.EditBoxGui;
import win.baruna.blockmeter.gui.OptionsGui;
import win.baruna.blockmeter.gui.SelectBoxGui;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
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

    private static ClientPlayerEntity getPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
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

    public static ModConfig getConfig() {
        return confMgr.getConfig();
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
    private final List<ClientMeasureBox> boxes = new ArrayList<>();

    /**
     * A Map of Lists of Boxes currently created by other Users, with Text being the
     * Username
     */
    private Map<String, List<ClientMeasureBox>> otherUsersBoxes;

    /**
     * The QuickMenu for changing of Color etc.
     */
    private final OptionsGui quickMenu;

    /**
     * The QuickMenu for selecting on of multiple Boxes.
     */
    private final SelectBoxGui selectBoxGui;
    private final EditBoxGui editBoxGui;

    public BlockMeterClient() {
        active = false;
        quickMenu = new OptionsGui();
        selectBoxGui = new SelectBoxGui();
        editBoxGui = new EditBoxGui();
        otherUsersBoxes = null;
        BlockMeterClient.instance = this;
    }

    /**
     * Disables BlockMeter
     */
    public void disable() {
        active = false;
        currentItem = null;
        boxes.clear();
        if (confMgr.getConfig().deleteBoxesOnDisable) {
            clear();
        }
    }

    /**
     * Resets Blockmeter to be used in another World
     */
    public void reset() {
        otherUsersBoxes = null;
        boxes.clear();
        disable();

        // Resets Color to always start with white in another world
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
        boolean hasBox = !boxes.isEmpty();
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
        if (this.boxes.isEmpty())
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

    public void renderOverlay(WorldRenderContext context) {
        final Identifier currentDimension = getPlayer().clientWorld.getRegistryKey().getValue();

        final ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        // MEH! but this seems to be needed to get the first background
        // rectangle
        //        client.textRenderer.draw("XXX", stack,  -100, -100, 0);

        if (this.active || cfg.showBoxesWhenDisabled)
            if (cfg.showOtherUsersBoxes) {
                if (otherUsersBoxes != null && !otherUsersBoxes.isEmpty()) {
                    this.otherUsersBoxes.forEach((playerText, boxList) -> boxList.forEach(box -> box.render(
                            context, currentDimension, Text.literal(playerText))));
                    this.boxes.forEach(box -> {
                        if (!box.isFinished())
                            box.render(context, currentDimension);
                    });
                }
                if (!cfg.sendBoxes)
                    this.boxes.forEach(box -> {
                        if (box.isFinished())
                            box.render(context, currentDimension, getPlayer().getDisplayName());
                        else
                            box.render(context, currentDimension);
                    });
            } else
                this.boxes.forEach(box -> box.render(context, currentDimension));

    }

    /**
     * Gets Triggered when the Player disconnects from the Server
     */
    public void onDisconnected(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        reset();
    }

    /**
     * Gets Triggered when the Player connects to the Server
     */
    private void onConnected(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender,
                             MinecraftClient minecraftClient) {
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
        final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blockmeter.assign",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.blockmeter.key"));
        final KeyBinding keyBindingMenu = new KeyBinding("key.blockmeter.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT, "category.blockmeter.key");
        KeyBindingHelper.registerKeyBinding(keyBindingMenu);

        final KeyBinding keyBindingMeasureWithItem = new KeyBinding("key.blockmeter.useItem", -1,
                "category.blockmeter.key");
        KeyBindingHelper.registerKeyBinding(keyBindingMeasureWithItem);
        final KeyBinding keyBindingMeasure = new KeyBinding("key.blockmeter.measure", InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_4, "category.blockmeter.key");
        KeyBindingHelper.registerKeyBinding(keyBindingMeasure);

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(this::renderOverlay);

        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnected);
        ClientPlayConnectionEvents.JOIN.register(this::onConnected);
        ClientPlayNetworking.registerGlobalReceiver(BoxPayload.ID, this::handleServerBoxList);

        AtomicBoolean measureWithItemDown = new AtomicBoolean(false);

        // This is ugly I know, but I did not find something better
        // (Issue in AutoConfig https://github.com/shedaniel/AutoConfig/issues/13)
        confMgr = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            if (keyBinding.wasPressed()) {
                if (Screen.hasShiftDown()) {
                    if (undo())
                        getPlayer().sendMessage(Text.translatable("blockmeter.clearLast"), true);
                } else if (Screen.hasControlDown()) {
                    if (clear())
                        getPlayer().sendMessage(Text.translatable("blockmeter.clearAll"), true);
                } else if (this.active) {
                    disable();
                    getPlayer().sendMessage(Text.translatable("blockmeter.toggle.off"), true);
                } else {
                    active = true;
                    ItemStack itemStack = getPlayer().getMainHandStack();
                    currentItem = itemStack.getItem();
                    getPlayer().sendMessage(
                            Text.translatable("blockmeter.toggle.on", itemStack.getItemName()),
                            true);
                }
            }

            if (keyBindingMenu.wasPressed() && active) {
                MinecraftClient.getInstance().setScreen(this.quickMenu);
            }

            if (keyBindingMeasure.wasPressed()) {
                this.active = true;
                raycastBlock().ifPresent(this::onBlockMeterClick);
            }

            // Updates Selection preview
            if (this.active && !this.boxes.isEmpty()) {
                final ClientMeasureBox currentBox = getCurrentBox();
                if (currentBox != null) {
                    this.raycastBlock().ifPresent(currentBox::setBlockEnd);
                }
            }

            if (this.active) {
                var key = KeyBindingHelper.getBoundKeyOf(keyBindingMeasureWithItem);
                var pressed = false;
                if (key.getCode() == -1) {
                    pressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 1) == 1;
                } else {
                    switch (key.getCategory()) {
                        case KEYSYM, SCANCODE -> pressed = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow()
                                .getHandle(), key.getCode()) == 1;
                        case MOUSE -> pressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow()
                                .getHandle(), key.getCode()) == 1;
                    }
                }
                if (pressed) {
                    if (!measureWithItemDown.get()) {
                        measureWithItemDown.set(true);
                        if (getPlayer().getMainHandStack().getItem().equals(this.currentItem)) {
                            raycastBlock().ifPresent(this::onBlockMeterClick);
                        }
                    }
                } else {
                    measureWithItemDown.set(false);
                }
            }
        });

        UseItemCallback.EVENT.register((playerEntity, world, _hand) -> {
            if (this.active && playerEntity.getMainHandStack().getItem().equals(this.currentItem)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseBlockCallback.EVENT.register((playerEntity, world, _hand, _block) -> {
            if (this.active && playerEntity.getMainHandStack().getItem().equals(this.currentItem)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        AttackBlockCallback.EVENT.register(((player, world, hand, pos, direction) -> {
            var inside = this.boxes.stream()
                    .filter(box -> box.miningRestriction == ClientMeasureBox.MiningRestriction.Inside)
                    .anyMatch(box -> !box.contains(pos));
            var outside = this.boxes.stream()
                    .filter(box -> box.miningRestriction == ClientMeasureBox.MiningRestriction.Outside)
                    .anyMatch(box -> box.contains(pos));
            if (!Screen.hasShiftDown() && (inside || outside)) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        }));
    }

    private Optional<BlockPos> raycastBlock() {
        var camera = MinecraftClient.getInstance().getCameraEntity();
        if (camera == null) {
            return Optional.empty();
        }
        final HitResult rayHit = camera.raycast(BlockMeterClient.getConfig().reach, 0.0f, false);
        if (rayHit.getType() == HitResult.Type.BLOCK) {
            final BlockHitResult blockHitResult = (BlockHitResult) rayHit;
            return Optional.of(blockHitResult.getBlockPos());
        }
        return Optional.empty();
    }

    public void editBox(ClientMeasureBox box, BlockPos block) {
        this.editBoxGui.setBox(box);
        this.editBoxGui.setBlock(block);
        MinecraftClient.getInstance().setScreen(this.editBoxGui);
    }

    /**
     * Handles the right click Event for creating and confirming new Measuring-Boxes
     */
    private void onBlockMeterClick(final BlockPos block) {
        ClientMeasureBox currentBox = getCurrentBox();

        if (currentBox == null) {
            if (Screen.hasShiftDown()) {
                ClientMeasureBox[] boxes = findBoxes(block);
                switch (boxes.length) {
                    case 0:
                        break;
                    case 1:
                        editBox(boxes[0], block);
                        break;
                    default:
                        this.selectBoxGui.setBoxes(boxes);
                        this.selectBoxGui.setBlock(block);
                        MinecraftClient.getInstance().setScreen(this.selectBoxGui);
                        break;
                }
            } else {
                final ClientMeasureBox box = ClientMeasureBox.getBox(block,
                        getPlayer().getWorld().getRegistryKey().getValue());
                this.boxes.add(box);
            }
        } else {
            currentBox.setBlockEnd(block);
            currentBox.setFinished();
            sendBoxList();
        }
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
        ClientPlayNetworking.send(new BoxPayload(Map.of("", new ArrayList<>(boxes))));
    }

    /**
     * handles the BoxList of other Players
     */
    private void handleServerBoxList(BoxPayload payload, ClientPlayNetworking.Context context) {
        context.client().executeTask(() -> otherUsersBoxes = payload.receivedBoxes().entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().stream().map(ClientMeasureBox::new)
                        .toList()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
    }

}
