package win.baruna.blockmeter.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.baruna.blockmeter.BlockMeterClient;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerDisconnectMixin {

    @Inject(method="onGameJoin", at=@At("RETURN"))
    private void onConnectedToServerEvent(GameJoinS2CPacket packet, CallbackInfo cbi) {
        BlockMeterClient.instance.connected();
    }
    
    @Inject(method="onDisconnected", at=@At("HEAD"))
    public void onDisconnectFromServerEvent(CallbackInfo cbi) {
        BlockMeterClient.instance.disconnected();
    }
}
