package fun.ksnb.multilogin.velocity.auth.vmsinbound;

import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import net.kyori.adventure.text.Component;

public class InitialInboundConnectionProxy implements Disconnectable {
    private final InitialInboundConnection connection;

    private InitialInboundConnectionProxy(InitialInboundConnection connection) {
        this.connection = connection;
    }

    public static Disconnectable of(Object obj) {
        return new InitialInboundConnectionProxy((InitialInboundConnection) obj);
    }

    @Override
    public void disconnect(Component reason) {
        connection.disconnect(reason);
    }

    @Override
    public String toString() {
        return "InitialInboundConnectionProxy{" + "connection=" + connection +
                '}';
    }
}
