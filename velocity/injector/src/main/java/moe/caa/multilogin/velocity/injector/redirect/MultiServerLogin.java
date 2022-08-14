package moe.caa.multilogin.velocity.injector.redirect;

import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.velocity.injector.proxy.IdentifiedKeyInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * 擦除登录验证签名的包
 */
@AllArgsConstructor
public class MultiServerLogin extends ServerLogin {
    private final MultiCoreAPI multiCoreAPI;

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        if (getPlayerKey() != null) {
            setPlayerKey((IdentifiedKey) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{IdentifiedKey.class}, new IdentifiedKeyInvocationHandler(getPlayerKey())));
        }

        return super.handle(handler);
    }
}
