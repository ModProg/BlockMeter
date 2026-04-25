package win.baruna.blockmeter;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public interface InputUtils {
    default Window window() {
        return Minecraft.getInstance().getWindow();
    }

    default boolean isKey(int key) {
        return InputConstants.isKeyDown(window(), key);
    }

    default boolean isShift() {
        return isKey(InputConstants.KEY_LSHIFT) || isKey(InputConstants.KEY_RSHIFT);
    }

    default boolean isCtrl() {
        return isKey(InputConstants.KEY_LCONTROL) || isKey(InputConstants.KEY_RCONTROL);
    }
}