package moe.caa.multilogin.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public interface IPlugin {
    File getPluginDataFolder();

    IConfiguration getPluginConfig();

    void savePluginDefaultConfig();

    void reloadPluginConfig();

    IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException;

    InputStream getPluginResource(String path);

    void kickPlayer(UUID uuid, String msg);

    Logger getPluginLogger();

    String getVersion();

    void runTaskAsyncLater(Runnable run, long delay);

    void runTaskAsyncTimer(Runnable run, long delay, long per);

    void runTask(Runnable run, long delay);

    Map<UUID, String> getOnlineList();
}
