package win.baruna.blockmeter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

import java.util.function.Function;
import java.util.function.Supplier;

public interface InputUtils {
    default Window window() {
        return MinecraftClient.getInstance().getWindow();
    }

    default boolean isKey(int key) {
        return InputUtil.isKeyPressed(window(), key);
    }

    default boolean isShift() {
        return isKey(InputUtil.GLFW_KEY_LEFT_SHIFT) || isKey(InputUtil.GLFW_KEY_RIGHT_SHIFT);
    }

    default boolean isCtrl() {
        return isKey(InputUtil.GLFW_KEY_LEFT_CONTROL) || isKey(InputUtil.GLFW_KEY_RIGHT_CONTROL);
    }
}