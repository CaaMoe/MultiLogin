package moe.caa.multilogin.bukkit.nms.v1_16_R3.impl;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.nms.IncompatibleException;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.NetworkManager;

import java.lang.reflect.Field;
import java.util.Map;

public class BukkitUserLogin extends BaseUserLogin {
    private static final Class<?> LOGIN_LISTENER_CLASS;
    private static final Class<?> LOGIN_LISTENER_LOGIN_HANDLER_CLASS;

    private static final Field LOGIN_LISTENER_NETWORK_MANAGER_FIELD;
    private static final Field LOGIN_LISTENER_GAME_PROFILE_FIELD;

    static {
        try {
            LOGIN_LISTENER_CLASS = LoginListener.class;
            LOGIN_LISTENER_LOGIN_HANDLER_CLASS = LoginListener.LoginHandler.class;
            LOGIN_LISTENER_NETWORK_MANAGER_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("networkManager"), true);
            LOGIN_LISTENER_GAME_PROFILE_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("i"), true);
        } catch (Exception e) {
            throw new IncompatibleException(e);
        }
    }

    private final LoginListener vanHandler;

    public BukkitUserLogin(LoginListener vanHandler, String username, String serverId, String ip) {
        super(username, serverId, ip);
        this.vanHandler = vanHandler;
    }

    @Override
    public void disconnect(String message) {
        vanHandler.disconnect(message);
    }

    @SneakyThrows
    @Override
    public void finish(HasJoinedResponse response) {
        if (!((NetworkManager) LOGIN_LISTENER_NETWORK_MANAGER_FIELD.get(vanHandler)).isConnected()) return;
        LOGIN_LISTENER_GAME_PROFILE_FIELD.set(vanHandler, genGameProfile(response));
        LoginListener.LoginHandler o1 = (LoginListener.LoginHandler) LOGIN_LISTENER_LOGIN_HANDLER_CLASS.getDeclaredConstructors()[0].newInstance(vanHandler);
        o1.fireEvents();
    }


    private GameProfile genGameProfile(HasJoinedResponse response) {
        GameProfile result = new GameProfile(response.getId(), response.getName());
        if (response.getPropertyMap() != null) {
            for (Map.Entry<String, Property> entry : response.getPropertyMap().entrySet()) {
                result.getProperties().put(entry.getKey(),
                        new com.mojang.authlib.properties.Property(entry.getValue().getName(), entry.getValue().getValue(), entry.getValue().getSignature()));
            }
        }
        return result;
    }
}
