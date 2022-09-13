package moe.caa.multilogin.fabric.mixin;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.fabric.event.LoginStateOnKeyEvent;
import moe.caa.multilogin.fabric.event.LoginStatePlayerDisconnectEvent;
import moe.caa.multilogin.fabric.inject.mixin.IServerLoginNetworkHandler_MLA;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 此处修改为，修改提示
 */
@Mixin(ServerLoginNetworkHandler.class)
public abstract class MixinServerLoginNetworkHandler implements IServerLoginNetworkHandler_MLA {

    @Shadow
    @Final
    public ClientConnection connection;
    @Shadow
    @Nullable GameProfile profile;

    @Shadow
    public abstract String getConnectionInfo();

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Text reason, CallbackInfo ci) {
        LoginStatePlayerDisconnectEvent.EventData eventData = new LoginStatePlayerDisconnectEvent.EventData((ServerLoginNetworkHandler) (Object) this, reason);
        LoginStatePlayerDisconnectEvent.INSTANCE.invoker().accept(eventData);
        if (reason.equals(eventData.getDisconnectText())) return;
        reason = eventData.getDisconnectText();
        ci.cancel();
        try {
            LoggerProvider.getLogger().info(String.format("Disconnecting %s: %s", this.getConnectionInfo(), reason.getString()));
            this.connection.send(new LoginDisconnectS2CPacket(reason));
            this.connection.disconnect(reason);
        } catch (Exception var3) {
            LoggerProvider.getLogger().error("Error whilst disconnecting player", var3);
        }
    }

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(LoginKeyC2SPacket packet, CallbackInfo ci) {
        LoginStateOnKeyEvent.INSTANCE.invoker().accept(new LoginStateOnKeyEvent.EventData((ServerLoginNetworkHandler) (Object) this, packet));
    }

    @Override
    public GameProfile mlHandler_getGameProfile() {
        return profile;
    }
}
