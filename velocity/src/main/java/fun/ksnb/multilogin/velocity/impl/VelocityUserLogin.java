package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class VelocityUserLogin extends BaseUserLogin {
    private static MethodHandle INITIALIZE_PLAYER_METHOD;
    private final LoginSessionHandler sessionHandler;
    private final Disconnectable disconnectable;

    public VelocityUserLogin(String username, String serverId, String ip, LoginSessionHandler sessionHandler, Disconnectable disconnectable) {
        super(username, serverId, ip);
        this.sessionHandler = sessionHandler;
        this.disconnectable = disconnectable;
    }

    public static void init() throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        INITIALIZE_PLAYER_METHOD = lookup.unreflect(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredMethod("initializePlayer", GameProfile.class, boolean.class), true));
    }

    @Override
    public void disconnect(String message) {
        disconnectable.disconnect(Component.text(message));
    }

    @Override
    public void finish(HasJoinedResponse response) {
        try {
            INITIALIZE_PLAYER_METHOD.invoke(sessionHandler, generateLoginResult(response), true);
        } catch (Throwable e) {
            disconnectable.disconnect(
                    Component.text(MultiLoginVelocityPluginBootstrap.getInstance().getCore().getLanguageHandler().getMessage("auth_error", FormatContent.empty()))
            );
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred at the end of processing login.", e);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "sessionHandler: " + sessionHandler);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "connection: " + disconnectable);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "userLogin: " + this);
        }
    }

    private GameProfile generateLoginResult(HasJoinedResponse response) {
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

    private GameProfile.Property generateProperty(Property property) {
        return new GameProfile.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
