package win.baruna.blockmeter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import win.baruna.blockmeter.BlockMeterClient;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerConnectionMixin {

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onConnectedToServerEvent(GameJoinS2CPacket packet, CallbackInfo cbi) {
        BlockMeterClient.instance.onConnected();
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    public void onDisconnectedFromServerEvent(DisconnectS2CPacket packet, CallbackInfo cbi) {
        BlockMeterClient.instance.onDisconnected();
    }
}
