package moe.caa.multilogin.bungee;

import com.google.gson.Gson;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISchedule;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MultiLoginBungee extends Plugin implements IPlugin {

    @Override
    public void onEnable() {
        MultiCore.init(this);
    }

    @Override
    public InputStream getJarResource(String path) {
        return getResourceAsStream(path);
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return getProxy().getPlayers().stream().map(BungeeSender::new).collect(Collectors.toList());
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public ISchedule getSchedule() {
        return null;
    }

    @Override
    public boolean isOnlineMode() {
        return getProxy().getConfig().isOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        return new BungeeSender(getProxy().getPlayer(uuid));
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) ret.add(new BungeeSender(player));
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return null;
    }

    @Override
    public Type authResultType() {
        return null;
    }
}
