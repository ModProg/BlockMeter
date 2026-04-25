package win.baruna.blockmeter;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
public class BlockMeterClient implements ClientModInitializer, InputUtils {
    /// Currently running Instance of BlockMeterClient
    private static BlockMeterClient instance;
    private static ConfigManager<ModConfig> confMgr;
    /// The List of Measuring-Boxes currently created by the current User
    private final List<ClientMeasureBox> boxes = new ArrayList<>();
    /// The QuickMenu for changing of Color etc.
    private final OptionsGui quickMenu;
    /// The QuickMenu for selecting on of multiple Boxes.
    private final SelectBoxGui selectBoxGui;
    private final EditBoxGui editBoxGui;
    /// The current state of the BlockMeter (activated/deactivated)
    private boolean active;
    /// The Item selected as BlockMeter
    private Item currentItem;
    /// A Map of Lists of Boxes currently created by other Users, with Text being the
    /// Username
    private Map<String, List<ClientMeasureBox>> otherUsersBoxes;

    public BlockMeterClient() {
        active = false;
        quickMenu = new OptionsGui();
        selectBoxGui = new SelectBoxGui();
        editBoxGui = new EditBoxGui();
        otherUsersBoxes = null;
        BlockMeterClient.instance = this;
    }

    /// Currently running Instance of BlockMeterClient
    public static BlockMeterClient getInstance() {
        return instance;
    }

    private static LocalPlayer getPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    /// Accessor for the ModConfigManager
    public static ConfigManager<ModConfig> getConfigManager() {
        return confMgr;
    }

    public static ModConfig getConfig() {
        return confMgr.getConfig();
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
        if (this.boxes.isEmpty()) return false;

        this.boxes.remove(this.boxes.size() - 1);
        sendBoxList();

        ModConfig cfg = confMgr.getConfig();
        if (cfg.incrementColor) {
            cfg.colorIndex = Math.floorMod(cfg.colorIndex - 1, DyeColor.values().length);
            confMgr.save();
        }

        return true;
    }

    public void renderOverlay(LevelRenderContext context) {
        final Identifier currentDimension = getPlayer().level().dimension().identifier();

        final ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if ((this.active || cfg.showBoxesWhenDisabled)) {
            if (cfg.showOtherUsersBoxes) {
                if (otherUsersBoxes != null && !otherUsersBoxes.isEmpty()) {
                    this.otherUsersBoxes.forEach((playerText, boxList) -> boxList.forEach(box -> box.render(context, currentDimension, playerText)));
                    this.boxes.forEach(box -> {
                        if (!box.isFinished()) box.render(context, currentDimension);
                    });
                }
                if (!cfg.sendBoxes) this.boxes.forEach(box -> {
                    if (box.isFinished()) box.render(context, currentDimension, getPlayer().getDisplayName().tryCollapseToString());
                    else box.render(context, currentDimension);
                });
            } else this.boxes.forEach(box -> box.render(context, currentDimension));
        }

    }

    /**
     * Gets Triggered when the Player disconnects from the Server
     */
    public void onDisconnected(ClientPacketListener clientPlayNetworkHandler, Minecraft minecraftClient) {
        reset();
    }

    /**
     * Gets Triggered when the Player connects to the Server
     */
    private void onConnected(ClientPacketListener clientPlayNetworkHandler, PacketSender packetSender, Minecraft minecraftClient) {
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
        var category = KeyMapping.Category.register(Identifier.parse("category.blockmeter.key"));
        final KeyMapping keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blockmeter.assign", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category));
        final KeyMapping keyBindingMenu = new KeyMapping("key.blockmeter.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, category);
        KeyMappingHelper.registerKeyMapping(keyBindingMenu);

        final KeyMapping keyBindingMeasureWithItem = new KeyMapping("key.blockmeter.useItem", -1, category);
        KeyMappingHelper.registerKeyMapping(keyBindingMeasureWithItem);
        final KeyMapping keyBindingMeasure = new KeyMapping("key.blockmeter.measure", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, category);
        KeyMappingHelper.registerKeyMapping(keyBindingMeasure);

        LevelRenderEvents.BEFORE_GIZMOS.register(this::renderOverlay);

        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnected);
        ClientPlayConnectionEvents.JOIN.register(this::onConnected);
        ClientPlayNetworking.registerGlobalReceiver(BoxPayload.ID, this::handleServerBoxList);

        AtomicBoolean measureWithItemDown = new AtomicBoolean(false);

        // This is ugly I know, but I did not find something better
        // (Issue in AutoConfig https://github.com/shedaniel/AutoConfig/issues/13)
        confMgr = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            if (keyBinding.consumeClick()) {
                if (isShift()) {
                    if (undo()) getPlayer().sendOverlayMessage(Component.translatable("blockmeter.clearLast"));
                } else if (isCtrl()) {
                    if (clear()) getPlayer().sendOverlayMessage(Component.translatable("blockmeter.clearAll"));
                } else if (this.active) {
                    disable();
                    getPlayer().sendOverlayMessage(Component.translatable("blockmeter.toggle.off"));
                } else {
                    active = true;
                    ItemStack itemStack = getPlayer().getMainHandItem();
                    currentItem = itemStack.getItem();
                    getPlayer().sendOverlayMessage(Component.translatable("blockmeter.toggle.on", itemStack.getItemName()));
                }
            }

            if (keyBindingMenu.consumeClick() && active) {
                Minecraft.getInstance().setScreen(this.quickMenu);
            }

            if (keyBindingMeasure.consumeClick()) {
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
                var key = KeyMappingHelper.getBoundKeyOf(keyBindingMeasureWithItem);
                var pressed = false;
                if (key.getValue() == -1) {
                    pressed = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), 1) == 1;
                } else {
                    switch (key.getType()) {
                        case KEYSYM, SCANCODE ->
                                pressed = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), key.getValue()) == 1;
                        case MOUSE ->
                                pressed = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), key.getValue()) == 1;
                    }
                }
                if (pressed) {
                    if (!measureWithItemDown.get()) {
                        measureWithItemDown.set(true);
                        if (getPlayer().getMainHandItem().getItem().equals(this.currentItem)) {
                            raycastBlock().ifPresent(this::onBlockMeterClick);
                        }
                    }
                } else {
                    measureWithItemDown.set(false);
                }
            }
        });

        UseItemCallback.EVENT.register((playerEntity, world, _hand) -> {
            if (this.active && playerEntity.getMainHandItem().getItem().equals(this.currentItem)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((playerEntity, world, _hand, _block) -> {
            if (this.active && playerEntity.getMainHandItem().getItem().equals(this.currentItem)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register(((player, world, hand, pos, direction) -> {
            var inside = this.boxes.stream().filter(box -> box.miningRestriction == ClientMeasureBox.MiningRestriction.Inside).anyMatch(box -> !box.contains(pos));
            var outside = this.boxes.stream().filter(box -> box.miningRestriction == ClientMeasureBox.MiningRestriction.Outside).anyMatch(box -> box.contains(pos));
            if (!isShift() && (inside || outside)) {
                return InteractionResult.FAIL;
            } else {
                return InteractionResult.PASS;
            }
        }));
    }

    private Optional<BlockPos> raycastBlock() {
        var camera = Minecraft.getInstance().getCameraEntity();
        if (camera == null) {
            return Optional.empty();
        }
        final HitResult rayHit = camera.pick(BlockMeterClient.getConfig().reach, 0.0f, false);
        if (rayHit.getType() == HitResult.Type.BLOCK) {
            final BlockHitResult blockHitResult = (BlockHitResult) rayHit;
            return Optional.of(blockHitResult.getBlockPos());
        }
        return Optional.empty();
    }

    public void editBox(ClientMeasureBox box, BlockPos block) {
        this.editBoxGui.setBox(box);
        this.editBoxGui.setBlock(block);
        Minecraft.getInstance().setScreen(this.editBoxGui);
    }

    /**
     * Handles the right click Event for creating and confirming new Measuring-Boxes
     */
    private void onBlockMeterClick(final BlockPos block) {
        ClientMeasureBox currentBox = getCurrentBox();

        if (currentBox == null) {
            if (isShift()) {
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
                        Minecraft.getInstance().setScreen(this.selectBoxGui);
                        break;
                }
            } else {
                final ClientMeasureBox box = ClientMeasureBox.getBox(block, getPlayer().level().dimension().identifier());
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
        if (!AutoConfig.getConfigHolder(ModConfig.class).getConfig().sendBoxes) return;
        ClientPlayNetworking.send(new BoxPayload(Map.of("", new ArrayList<>(boxes))));
    }

    /**
     * handles the BoxList of other Players
     */
    private void handleServerBoxList(BoxPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> otherUsersBoxes = payload.receivedBoxes().entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().stream().map(ClientMeasureBox::new).toList()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
    }

}
