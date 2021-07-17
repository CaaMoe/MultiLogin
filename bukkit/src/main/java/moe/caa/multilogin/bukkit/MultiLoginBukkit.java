package moe.caa.multilogin.bukkit;

import com.google.gson.Gson;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISchedule;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    public static ISchedule schedule;
    public static Gson authGson;
    public static final Map<UUID, Long> LOGIN_CACHE = new Hashtable<>();
    public static final Map<UUID, User> USER_CACHE = new Hashtable<>();

    @Override
    public void onEnable() {
        schedule = new BukkitSchedule(this);
        System.out.println(MultiCore.init(this));
    }

    @Override
    public InputStream getJarResource(String path) {
        return getResource(path);
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return getServer().getOnlinePlayers().stream().map(BukkitSender::new).collect(Collectors.toList());
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public ISchedule getSchedule() {
        return schedule;
    }

    @Override
    public boolean isOnlineMode() {
        return getServer().getOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        Player player = getServer().getPlayer(uuid);
        return player == null ? null : new BukkitSender(player);
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) ret.add(new BukkitSender(player));
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return authGson;
    }

    @Override
    public Type authResultType() {
        return HasJoinedMinecraftServerResponse.class;
    }
}
