package moe.caa.multilogin.bungee.impl;

import moe.caa.multilogin.core.IConfiguration;
import net.md_5.bungee.config.Configuration;

public class BungeeConfiguration implements IConfiguration {
    private final Configuration vanConfiguration;

    public BungeeConfiguration(Configuration vanConfiguration) {
        this.vanConfiguration = vanConfiguration;
    }

    public BungeeConfiguration() {
        vanConfiguration = new Configuration();
    }

    @Override
    public IConfiguration getConfigurationSection(String path) {
        return new BungeeConfiguration(vanConfiguration.getSection(path));
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
        return vanConfiguration.getKeys().toArray(new String[0]);
    }

    @Override
    public Configuration getVanConfiguration() {
        return vanConfiguration;
    }
}