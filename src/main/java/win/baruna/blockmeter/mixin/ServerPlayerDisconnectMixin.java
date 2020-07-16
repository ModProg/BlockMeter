package win.baruna.blockmeter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import win.baruna.blockmeter.BlockMeterServer;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayerDisconnectMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    public void removePlayerFromBoxList(Text reason, CallbackInfo ci) {
        BlockMeterServer.removePlayer(player);
    }
}
