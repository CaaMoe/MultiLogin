package fun.ksnb.multilogin.velocity.injector.redirect;

import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import fun.ksnb.multilogin.velocity.injector.proxy.IdentifiedKeyInvocationHandler;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.main.MultiCoreAPI;

import java.lang.reflect.Proxy;

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
