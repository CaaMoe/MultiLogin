package moe.caa.multilogin.bukkit.auth;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import moe.caa.multilogin.bukkit.impl.BukkitUserLogin;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkitPluginBootstrap;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.CachedHashSet;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BukkitAuthCore {
    @Getter
    private static final CachedHashSet<BukkitUserLogin> loginCachedHashSet = new CachedHashSet<>(10000);

    @Getter
    private static final UUID DIRTY_UUID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

    public GameProfile doAuth(GameProfile user, String serverId, InetAddress address) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            BukkitUserLogin login = new BukkitUserLogin(user.getName(), serverId, address == null ? null : address.getHostAddress(), latch);
            MultiLoginBukkitPluginBootstrap.getInstance().getCore().getAuthCore().doAuth(login);
            latch.await(MultiLoginBukkitPluginBootstrap.getInstance().getCore().getConfig().getServicesTimeOut(), TimeUnit.MILLISECONDS);
            loginCachedHashSet.add(login);
            return generate(login.getResponse(), user.getName());
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while processing login data.", e);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "GameProfile: " + user);
            return new GameProfile(DIRTY_UUID, user.getName());
        }
    }

    private GameProfile generate(HasJoinedResponse response, String username) {
        if (response == null || !response.isSucceed()) return new GameProfile(DIRTY_UUID, username);

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
