package moe.caa.multilogin.fabric.mixin;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.fabric.auth.MultiLoginGameProfile;
import moe.caa.multilogin.fabric.main.MultiLoginFabricPluginBootstrap;
import moe.caa.multilogin.fabric.mixininject.IEnhance_ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;

@Mixin(targets = {
        "net.minecraft.server.network.ServerLoginNetworkHandler$1"
}
)
public abstract class MixinServerLoginNetworkHandler_OnKey_Authenticator_ModifyHasJoined {

    @Shadow(aliases = "field_14176")
    private IEnhance_ServerLoginNetworkHandler handler;

    @Shadow
    protected abstract InetAddress getClientAddress();

    @Inject(method = "run()V",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;hasJoinedServer" +
                            "(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/net/InetAddress;)Lcom/mojang/authlib/GameProfile;",
                    remap = false, shift = At.Shift.AFTER),
            cancellable = true)
    private void onAsyncPreLogin(CallbackInfo ci) {
        GameProfile profile = handler.multiLogin_getProfile();
        if (profile instanceof MultiLoginGameProfile) {
            MultiLoginGameProfile mgp = (MultiLoginGameProfile) profile;
            if (mgp.getDisconnectMessage() == null) {
                return;
            }
            disconnectAndReturn(mgp.getDisconnectMessage(), ci);
            return;
        }

        disconnectAndReturn(MultiLoginFabricPluginBootstrap.getInstance().getCore().getLanguageHandler().getMessage("auth_fabric_invalid_login"), ci);
    }

    private void disconnectAndReturn(String disconnectMessage, CallbackInfo ci) {
        handler.getAsServerLoginNetworkHandler().disconnect(new LiteralText(disconnectMessage));
        ci.cancel();
    }
}
