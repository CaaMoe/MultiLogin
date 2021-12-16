package fun.ksnb.multilogin.velocity.auth;

import fun.ksnb.multilogin.velocity.auth.vmsinbound.InitialInboundConnectionProxy;
import fun.ksnb.multilogin.velocity.auth.vmsinbound.LoginInboundConnectionProxy;
import net.kyori.adventure.text.Component;

public interface Disconnectable {
    static Disconnectable generateDisconnectable(Object obj) {
        if (obj.getClass().getName().endsWith("InitialInboundConnection")) {
            return InitialInboundConnectionProxy.of(obj);
        }
        return LoginInboundConnectionProxy.of(obj);
    }

    void disconnect(Component reason);
}
