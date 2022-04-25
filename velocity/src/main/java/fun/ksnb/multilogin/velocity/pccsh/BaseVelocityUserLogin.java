package fun.ksnb.multilogin.velocity.pccsh;

import com.velocitypowered.api.util.GameProfile;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.impl.BaseUserLogin;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseVelocityUserLogin extends BaseUserLogin {
    protected final Disconnectable disconnectable;

    protected BaseVelocityUserLogin(String username, String serverId, String ip, Disconnectable disconnectable) {
        super(username, serverId, ip);
        this.disconnectable = disconnectable;
    }

    protected GameProfile generateLoginResult(HasJoinedResponse response) {
        List<Property> values = new ArrayList<>(response.getPropertyMap().values());

        List<GameProfile.Property> properties = new ArrayList<>();
        for (Property value : values) {
            properties.add(generateProperty(value));
        }

        return new GameProfile(
                response.getId().toString().replace("-", ""),
                response.getName(),
                properties
        );
    }

    protected GameProfile.Property generateProperty(Property property) {
        return new GameProfile.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
