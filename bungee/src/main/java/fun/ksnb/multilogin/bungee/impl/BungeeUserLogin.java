package fun.ksnb.multilogin.bungee.impl;

import lombok.SneakyThrows;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class BungeeUserLogin extends BaseUserLogin {
    private static MethodHandle LOGIN_PROFILE;
    private static MethodHandle NAME;
    private static MethodHandle UNIQUE_ID;
    private static MethodHandle FINISH;
    private final InitialHandler handler;

    public static void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Class<InitialHandler> INITIAL_HANDLER_CLASS = InitialHandler.class;

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        LOGIN_PROFILE = lookup.unreflectSetter(ReflectUtil.handleAccessible(ReflectUtil.getField(INITIAL_HANDLER_CLASS, LoginResult.class), true));
        NAME = lookup.unreflectSetter(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredField("name"), true));
        UNIQUE_ID = lookup.unreflectSetter(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredField("uniqueId"), true));
        FINISH = lookup.unreflect(ReflectUtil.handleAccessible(INITIAL_HANDLER_CLASS.getDeclaredMethod("finish"), true));
    }

    public BungeeUserLogin(String username, String serverId, String ip, InitialHandler handler) {
        super(username, serverId, ip);
        this.handler = handler;
    }

    @Override
    public void disconnect(String message) {
        handler.disconnect(message);
    }

    @Override
    @SneakyThrows
    public void finish(HasJoinedResponse response) {
        LOGIN_PROFILE.invoke(handler, generateLoginResult(response));
        UNIQUE_ID.invoke(handler, response.getId());
        NAME.invoke(handler, response.getName());

        FINISH.invoke(handler);
    }

    private LoginResult generateLoginResult(HasJoinedResponse response){
        return new LoginResult(
                response.getId().toString().replace("-", ""),
                response.getName(),
                response.getPropertyMap().values().stream().map(this::generateProperty).toArray(value -> new LoginResult.Property[0])
        );
    }

    private LoginResult.Property generateProperty(Property property){
        return new LoginResult.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
