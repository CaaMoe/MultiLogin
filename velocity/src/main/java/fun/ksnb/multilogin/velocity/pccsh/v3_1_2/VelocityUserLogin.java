package fun.ksnb.multilogin.velocity.pccsh.v3_1_2;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.AuthSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap;
import fun.ksnb.multilogin.velocity.pccsh.BaseVelocityUserLogin;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.exception.NoSuchEnumException;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class VelocityUserLogin extends BaseVelocityUserLogin {
    private static MethodHandle MC_CONNECTION_FIELD;
    private static MethodHandle SERVER_FIELD;
    private static MethodHandle INBOUND_FIELD;
    private static MethodHandle CURRENT_STATE_FIELD;
    private static MethodHandle AUTH_SESSION_HANDLER_CONSTRUCTOR;

    private static Object encryption_response_received;
    private final InitialLoginSessionHandler sessionHandler;

    public VelocityUserLogin(String username, String serverId, String ip, InitialLoginSessionHandler sessionHandler, Disconnectable disconnectable) {
        super(username, serverId, ip, disconnectable);
        this.sessionHandler = sessionHandler;
    }

    public VelocityUserLogin(String username, String serverId, String ip, Object sessionHandler, Disconnectable disconnectable) {
        super(username, serverId, ip, disconnectable);
        this.sessionHandler = (InitialLoginSessionHandler) sessionHandler;
    }

    public static void init() throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, NoSuchEnumException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MC_CONNECTION_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("mcConnection"), true));
        SERVER_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("server"), true));
        INBOUND_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("inbound"), true));
        CURRENT_STATE_FIELD = lookup.unreflectSetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("currentState"), true));
        AUTH_SESSION_HANDLER_CONSTRUCTOR = lookup.unreflectConstructor(ReflectUtil.handleAccessible(AuthSessionHandler.class.getDeclaredConstructor(VelocityServer.class, LoginInboundConnection.class, GameProfile.class, boolean.class), true));
        encryption_response_received = ReflectUtil.getEnumIns((Class<? extends Enum<?>>) Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler$LoginState"), "ENCRYPTION_RESPONSE_RECEIVED");
    }

    @Override
    public void disconnect(String message) {
        disconnectable.disconnect(Component.text(message));
    }

    @Override
    public void finish(HasJoinedResponse response) {
        try {
            final MinecraftConnection invoke = (MinecraftConnection) MC_CONNECTION_FIELD.invoke(sessionHandler);
            CURRENT_STATE_FIELD.invoke(sessionHandler, encryption_response_received);

            invoke.setSessionHandler(
                    (AuthSessionHandler) AUTH_SESSION_HANDLER_CONSTRUCTOR.invoke(
                            SERVER_FIELD.invoke(sessionHandler),
                            INBOUND_FIELD.invoke(sessionHandler),
                            generateLoginResult(response),
                            true
                    )
            );
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
