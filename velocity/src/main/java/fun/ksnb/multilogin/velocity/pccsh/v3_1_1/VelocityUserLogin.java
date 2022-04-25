package fun.ksnb.multilogin.velocity.pccsh.v3_1_1;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap;
import fun.ksnb.multilogin.velocity.pccsh.BaseVelocityUserLogin;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class VelocityUserLogin extends BaseVelocityUserLogin {
    private static MethodHandle INITIALIZE_PLAYER_METHOD;
    private final LoginSessionHandler sessionHandler;

    public VelocityUserLogin(String username, String serverId, String ip, LoginSessionHandler sessionHandler, Disconnectable disconnectable) {
        super(username, serverId, ip, disconnectable);
        this.sessionHandler = sessionHandler;
    }

    public VelocityUserLogin(String username, String serverId, String ip, Object sessionHandler, Disconnectable disconnectable) {
        super(username, serverId, ip, disconnectable);
        this.sessionHandler = (LoginSessionHandler) sessionHandler;
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
}
