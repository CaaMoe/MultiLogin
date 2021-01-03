package moe.caa.multilogin.bukkit;

import moe.caa.multilogin.core.IConfiguration;
import org.bukkit.configuration.ConfigurationSection;

public class BukkitConfiguration implements IConfiguration {
    private final ConfigurationSection vanConfiguration;

    public BukkitConfiguration(ConfigurationSection vanConfiguration) {
        this.vanConfiguration = vanConfiguration;
    }

    @Override
    public IConfiguration getConfigurationSection(String path) {
        return new BukkitConfiguration(vanConfiguration.getConfigurationSection(path));
    }

    @Override
    public String getString(String path) {
        return vanConfiguration.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return vanConfiguration.getString(path, def);
    }

    @Override
    public boolean getBoolean(String path) {
        return vanConfiguration.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return vanConfiguration.getBoolean(path, def);
    }

    @Override
    public long getLong(String path) {
        return vanConfiguration.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        return vanConfiguration.getLong(path, def);
    }

    @Override
    public void set(String path, Object value) {
        vanConfiguration.set(path, value);
    }

    @Override
    public String[] getKeys(boolean b) {
        return vanConfiguration.getKeys(b).toArray(new String[0]);
    }
}
