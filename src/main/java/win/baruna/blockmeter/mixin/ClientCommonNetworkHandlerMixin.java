package win.baruna.blockmeter.mixin;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.baruna.blockmeter.BlockMeterClient;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    public void onDisconnectedFromServerEvent(DisconnectS2CPacket packet, CallbackInfo cbi) {
        BlockMeterClient.getInstance().onDisconnected();
    }
}
