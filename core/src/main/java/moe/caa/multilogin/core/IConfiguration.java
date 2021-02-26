package moe.caa.multilogin.core;

public interface IConfiguration {
    IConfiguration getConfigurationSection(String path);

    String getString(String path);

    String getString(String path, String def);

    boolean getBoolean(String path);

    boolean getBoolean(String path, boolean def);

    long getLong(String path);

    long getLong(String path, long def);

    void set(String path, Object value);

    String[] getKeys(boolean b);
}
