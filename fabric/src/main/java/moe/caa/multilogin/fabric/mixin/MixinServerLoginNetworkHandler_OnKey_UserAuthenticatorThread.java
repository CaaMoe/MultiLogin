package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.fabric.inject.IServerLoginNetworkHandler;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.server.network.ServerLoginNetworkHandler$1"})
public class MixinServerLoginNetworkHandler_OnKey_UserAuthenticatorThread {

    @Shadow(aliases = "field_14176")
    private ServerLoginNetworkHandler handler;

    @Inject(method = "run()V",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;hasJoinedServer" +
                            "(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/net/InetAddress;)Lcom/mojang/authlib/GameProfile;",
                    remap = false, shift = At.Shift.AFTER),
            cancellable = true)
    private void onAsyncPreLogin(CallbackInfo ci) {
        if (((IServerLoginNetworkHandler) handler).getProfile() != null) {
            String msg = MultiLoginFabric.AUTH_CACHE.remove(Thread.currentThread());
            if (msg != null) {
                handler.disconnect(new LiteralText(msg));
                ci.cancel();
                return;
            }

            if (!MultiLoginFabric.plugin.onAsyncLoginSuccess(((IServerLoginNetworkHandler) handler).getProfile().getId(), ((IServerLoginNetworkHandler) handler).getProfile().getName())) {
                handler.disconnect(new LiteralText(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage(MultiLoginFabric.plugin.getMultiCore())));
                ci.cancel();
            }
        }
    }
}
