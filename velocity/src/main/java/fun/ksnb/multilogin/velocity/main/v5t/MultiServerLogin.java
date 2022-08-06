package fun.ksnb.multilogin.velocity.main.v5t;

import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import fun.ksnb.multilogin.velocity.main.v5t.proxy.IdentifiedKeyInvocationHandler;

import java.lang.reflect.Proxy;

public class MultiServerLogin extends ServerLogin {

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
