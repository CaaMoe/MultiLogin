package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.IConfiguration;
import org.bukkit.configuration.ConfigurationSection;

public class BukkitConfiguration implements IConfiguration {
    private final ConfigurationSection vanHandle;

    public BukkitConfiguration(ConfigurationSection vanHandle) {
        this.vanHandle = vanHandle;
    }

    @Override
    public IConfiguration getConfigurationSection(String path) {
        return new BukkitConfiguration(vanHandle.getConfigurationSection(path));
    }

    @Override
    public String getString(String path) {
        return vanHandle.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return vanHandle.getString(path, def);
    }

    @Override
    public boolean getBoolean(String path) {
        return vanHandle.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return vanHandle.getBoolean(path, def);
    }

    @Override
    public long getLong(String path) {
        return vanHandle.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        return vanHandle.getLong(path, def);
    }

    @Override
    public void set(String path, Object value) {
        vanHandle.set(path, value);
    }

    @Override
    public String[] getKeys(boolean b) {
        return vanHandle.getKeys(b).toArray(new String[0]);
    }

    @Override
    public Object getVanConfiguration() {
        return vanHandle;
    }
}
