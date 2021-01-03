package moe.caa.multilogin.bungee;

import moe.caa.multilogin.bungee.listener.BungeeListener;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.IPlugin;
import moe.caa.multilogin.core.MultiCore;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiLogin extends Plugin implements IPlugin {
    public static MultiLogin INSTANCE;
    public static File configFile ;
    private final Timer TIMER = new Timer("MultiLogin", true);
    private IConfiguration configuration;


    @Override
    public void onEnable() {
        MultiLogin.INSTANCE = this;
        configFile = new File(this.getPluginDataFolder(), "config.yml");
        MultiCore.setPlugin(this);
        BungeeCord.getInstance().getPluginManager().registerListener(this, new BungeeListener());
    }

    @Override
    public void onDisable() {
        TIMER.cancel();
    }

    @Override
    public File getPluginDataFolder() {
        return getDataFolder();
    }

    @Override
    public IConfiguration getPluginConfig() {
        return configuration;
    }

    @Override
    public void savePluginDefaultConfig() {
        if (!configFile.exists()) {
            try {
                InputStream input = getPluginResource("config.yml");
                FileOutputStream fOut = new FileOutputStream(configFile);
                byte[] buf = new byte[1024];
                int len;

                while((len = input.read(buf)) > 0) {
                    fOut.write(buf, 0, len);
                }
                fOut.flush();
                fOut.close();
                input.close();
            } catch (Exception e) {
                getMLPluginLogger().log(Level.SEVERE, "无法保存文件 " + configFile.getName());
            }
        }
    }

    @Override
    public void reloadPluginConfig() {
        try {
            configuration = new BungeeConfiguration(ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml")));
        } catch (Exception ignore){
            getMLPluginLogger().log(Level.SEVERE, "无法读取文件 " + configFile.getName());
        }
    }

    @Override
    public IConfiguration yamlLoadConfiguration(InputStreamReader reader) {
        return new BungeeConfiguration(ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader));
    }

    @Override
    public InputStream getPluginResource(String path) {
        return getResourceAsStream(path);
    }

    @Override
    public void kickPlayer(UUID uuid, String msg) {
        ProxiedPlayer player = BungeeCord.getInstance().getPlayer(uuid);
        if(player != null){
            player.disconnect(new TextComponent(msg));
        }
    }

    @Override
    public Logger getMLPluginLogger() {
        return getLogger();
    }

    @Override
    public void runTaskAsyncLater(Runnable run, long delay) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, delay * 50);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long per) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, delay * 50, per * 50);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void setPluginEnabled(boolean b) {
        MultiCore.save();
        BungeeCord.getInstance().stop("MultiLogin ERROR");
    }
}
