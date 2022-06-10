package fun.ksnb.multilogin.bungee.impl.lrp.v1;

import fun.ksnb.multilogin.bungee.impl.lrp.BaseBungeeUserLogin;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.util.ArrayList;
import java.util.List;

public class BungeeUserLogin extends BaseBungeeUserLogin {
    public BungeeUserLogin(String username, String serverId, String ip, InitialHandler handler) {
        super(username, serverId, ip, handler);
    }

    protected LoginResult generateLoginResult(HasJoinedResponse response) {
        List<Property> values = new ArrayList<>(response.getPropertyMap().values());
        net.md_5.bungee.protocol.Property[] properties = new net.md_5.bungee.protocol.Property[values.size()];
        for (int i = 0; i < values.size(); i++) {
            properties[i] = generateProperty(values.get(i));
        }
        return new LoginResult(
                response.getId().toString().replace("-", ""),
                response.getName(),
                properties
        );
    }

    private net.md_5.bungee.protocol.Property generateProperty(Property property) {
        return new net.md_5.bungee.protocol.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
