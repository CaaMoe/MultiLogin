package moe.caa.multilogin.core;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Logger;

public interface IPlugin {
    File getPluginDataFolder();

    IConfiguration getPluginConfig();

    void savePluginDefaultConfig();

    void reloadPluginConfig();

    IConfiguration yamlLoadConfiguration(InputStreamReader reader);

    InputStream getPluginResource(String path);

    void kickPlayer(UUID uuid, String msg);

    Logger getMLPluginLogger();
}
