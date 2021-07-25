package moe.caa.multilogin.fabric.mixin;

import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class MixinServerLoginNetworkHandler {

    @Inject(method = "onKey", at = @At(value = "NEW", target = "Thread"))
    private void onAsyncLogin(LoginKeyC2SPacket packet, CallbackInfo ci){
        System.out.println("啊啊啊啊啊啊啊啊啊");
    }
}
