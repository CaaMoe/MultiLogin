package fun.ksnb.multilogin.bungee.impl;

import fun.ksnb.multilogin.bungee.main.MultiLoginBungee;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class BungeeUserLogin extends BaseUserLogin {
    private static MethodHandle LOGIN_PROFILE;
    private static MethodHandle NAME;
    private static MethodHandle UNIQUE_ID;
    private static MethodHandle FINISH;
    private final InitialHandler handler;

    public BungeeUserLogin(String username, String serverId, String ip, InitialHandler handler) {
        super(username, serverId, ip);
        this.handler = handler;
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Class<InitialHandler> INITIAL_HANDLER_CLASS = InitialHandler.class;

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        LOGIN_PROFILE = lookup.unreflectSetter(ReflectUtil.handleAccessible(ReflectUtil.getField(INITIAL_HANDLER_CLASS, LoginResult.class), true));
        NAME = lookup.unreflectSetter(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredField("name"), true));
        UNIQUE_ID = lookup.unreflectSetter(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredField("uniqueId"), true));
        FINISH = lookup.unreflect(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredMethod("finish"), true));
    }

    @Override
    public void disconnect(String message) {
        handler.disconnect(message);
    }

    @Override
    public void finish(HasJoinedResponse response) {
        try {
            LOGIN_PROFILE.invoke(handler, generateLoginResult(response));
            UNIQUE_ID.invoke(handler, response.getId());
            NAME.invoke(handler, response.getName());
            FINISH.invoke(handler);
        } catch (Throwable throwable) {
            handler.disconnect(MultiLoginBungee.getInstance().getCore().getLanguageHandler().getMessage("auth_error", FormatContent.empty()));
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred at the end of processing login.", throwable);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "handler: " + handler);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "userLogin: " + this);
        }
    }

    private LoginResult generateLoginResult(HasJoinedResponse response) {
        List<Property> values = new ArrayList<>(response.getPropertyMap().values());
        LoginResult.Property[] properties = new LoginResult.Property[values.size()];
        for (int i = 0; i < values.size(); i++) {
            properties[i] = generateProperty(values.get(i));
        }
        return new LoginResult(
                response.getId().toString().replace("-", ""),
                response.getName(),
                properties
        );
    }

    private LoginResult.Property generateProperty(Property property) {
        return new LoginResult.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
