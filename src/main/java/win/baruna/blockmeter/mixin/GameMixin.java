// 
// Decompiled by Procyon v0.5.30
// 

package win.baruna.blockmeter.mixin;

import net.minecraft.client.ClientGameSession;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import win.baruna.blockmeter.BlockMeter;

@Mixin({ ClientGameSession.class })
public class GameMixin
{
    @Inject(method = { "<init>*" }, at = { @At("RETURN") })
    private void onSessionStarted(final ClientWorld clientWorld_1, final ClientPlayerEntity clientPlayerEntity_1, final ClientPlayNetworkHandler clientPlayNetworkHandler_1, final CallbackInfo info) {
        BlockMeter.instance.clear();
    }
}
