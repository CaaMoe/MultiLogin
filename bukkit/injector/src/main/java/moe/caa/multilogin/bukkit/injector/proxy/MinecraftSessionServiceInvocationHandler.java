package moe.caa.multilogin.bukkit.injector.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.Property;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;

public class MinecraftSessionServiceInvocationHandler implements InvocationHandler {
    private final MinecraftSessionService origin;

    public MinecraftSessionServiceInvocationHandler(MinecraftSessionService origin) {
        this.origin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.getName().contains("hasJoined")) {
            return method.invoke(origin, args);
        }
        GameProfile profile = ((GameProfile) args[0]);
        String serverId = (String) args[1];
        String ip = "";
        if (args.length == 3) {
            if (args[2] != null) {
                ip = ((InetAddress) args[2]).getHostAddress();
            }
        }
        AuthResult authResult = BukkitInjector.getApi().getAuthHandler().auth(profile.getName(), serverId, ip);
        if (authResult.isAllowed()) {
            return generate(authResult.getResponse());
        } else {
            Contents.getKickMessageEntryMap().put(profile.getName(), Contents.KickMessageEntry.of(authResult.getKickMessage()));
            return null;
        }
    }

    private GameProfile generate(moe.caa.multilogin.api.auth.GameProfile response) {
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
