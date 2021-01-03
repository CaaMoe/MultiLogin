package moe.caa.multilogin.bungee;

import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.IPlugin;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Logger;

public class MultiLogin extends Plugin implements IPlugin {

    @Override
    public void onEnable() {

    }

    @Override
    public File getPluginDataFolder() {
        return getDataFolder();
    }

    @Override
    public IConfiguration getPluginConfig() {
        return new BungeeConfiguration(null);
    }

    @Override
    public void savePluginDefaultConfig() {

    }

    @Override
    public void reloadPluginConfig() {

    }

    @Override
    public IConfiguration yamlLoadConfiguration(InputStreamReader reader) {
        return null;
    }

    @Override
    public InputStream getPluginResource(String path) {
        return null;
    }

    @Override
    public void kickPlayer(UUID uuid, String msg) {

    }

    @Override
    public Logger getMLPluginLogger() {
        return null;
    }
}
