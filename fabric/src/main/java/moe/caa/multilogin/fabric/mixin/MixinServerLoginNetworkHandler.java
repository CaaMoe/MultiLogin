package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.event.LoginStatePlayerDisconnectEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
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
public abstract class MixinServerLoginNetworkHandler {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract String getConnectionInfo();

    @Shadow @Final public ClientConnection connection;

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Text reason, CallbackInfo ci){
        LoginStatePlayerDisconnectEvent.EventData eventData = new LoginStatePlayerDisconnectEvent.EventData((ServerLoginNetworkHandler) (Object)this, reason);
        LoginStatePlayerDisconnectEvent.INSTANCE.invoker().accept(eventData);
        if(reason.equals(eventData.getDisconnectText())) return;
        ci.cancel();
        try {
            LOGGER.info((String)"Disconnecting {}: {}", this.getConnectionInfo(), reason.getString());
            this.connection.send(new LoginDisconnectS2CPacket(reason));
            this.connection.disconnect(reason);
        } catch (Exception var3) {
            LOGGER.error((String)"Error whilst disconnecting player", var3);
        }
    }
}
