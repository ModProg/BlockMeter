// 
// Decompiled by Procyon v0.5.30
// 

package win.baruna.blockmeter.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import win.baruna.blockmeter.BlockMeter;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(method="renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at=@At(value="INVOKE_STRING",
                   target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                   args= { "ldc=hand" }
            )
    )

    private void onRenderCenterLast(float partialTicks, final long nanoTime, MatrixStack stack, CallbackInfo info) {
        BlockMeter.instance.renderOverlay(partialTicks, stack);
    }
}
