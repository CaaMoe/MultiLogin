package moe.caa.multilogin.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.server.network.ServerLoginNetworkHandler$1"})
public abstract class MixinServerLoginNetworkHandler {


    @Inject(method = "run()V", at = @At(value = "HEAD"), cancellable = true)
    private void onLoginAsync(CallbackInfo callbackInfo) {
        System.out.println("a");

        callbackInfo.cancel();
    }
}
