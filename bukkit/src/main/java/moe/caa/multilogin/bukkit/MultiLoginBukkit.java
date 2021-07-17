package moe.caa.multilogin.bukkit;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISchedule;
import moe.caa.multilogin.core.impl.ISender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    private final ISchedule SCHEDULE = new BukkitSchedule(this);

    @Override
    public void onEnable() {
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
        return SCHEDULE;
    }
}
