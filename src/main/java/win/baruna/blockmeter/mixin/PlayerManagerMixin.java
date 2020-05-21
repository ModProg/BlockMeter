package win.baruna.blockmeter.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method="onPlayerConnect", at=@At("RETURN"))
    public void playerHasConnected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        // hm, not even used yet? maybe send the box list to this player?
    }
    
    
}
