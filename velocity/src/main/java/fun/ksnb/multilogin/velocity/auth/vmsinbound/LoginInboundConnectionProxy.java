package fun.ksnb.multilogin.velocity.auth.vmsinbound;

import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import net.kyori.adventure.text.Component;

public class LoginInboundConnectionProxy implements Disconnectable {
    private final LoginInboundConnection connection;

    private LoginInboundConnectionProxy(LoginInboundConnection connection) {
        this.connection = connection;
    }

    public static Disconnectable of(Object obj) {
        return new LoginInboundConnectionProxy((LoginInboundConnection) obj);
    }

    @Override
    public void disconnect(Component reason) {
        connection.disconnect(reason);
    }

    @Override
    public String toString() {
        return "LoginInboundConnectionProxy{" + "connection=" + connection +
                '}';
    }
}
