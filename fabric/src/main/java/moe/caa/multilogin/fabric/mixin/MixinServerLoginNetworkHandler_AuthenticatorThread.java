package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.event.PrepareAcceptLoginPlayerEvent;
import moe.caa.multilogin.fabric.inject.mixin.IServerLoginNetworkHandler_MLA;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {
        "net.minecraft.server.network.ServerLoginNetworkHandler$1"
})
public abstract class MixinServerLoginNetworkHandler_AuthenticatorThread {

    @Shadow(aliases = "field_14176")
    private ServerLoginNetworkHandler handler;

    @Inject(method = "run",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;hasJoinedServer(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/net/InetAddress;)Lcom/mojang/authlib/GameProfile;",
                    remap = false,
                    shift = At.Shift.AFTER))
    private void onRun(CallbackInfo ci) {
        IServerLoginNetworkHandler_MLA handlerMla = (IServerLoginNetworkHandler_MLA) handler;
        if (handlerMla.mlHandler_getGameProfile() != null) {
            if (handlerMla.mlHandler_getGameProfile().getId() != null) {
                PrepareAcceptLoginPlayerEvent.INSTANCE.invoker().accept(new PrepareAcceptLoginPlayerEvent.EventData(handler));
            }
        }
    }
}
