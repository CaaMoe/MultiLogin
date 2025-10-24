package moe.caa.multilogin.common.internal;

import moe.caa.multilogin.common.internal.logger.KLogger;
import moe.caa.multilogin.common.internal.manager.OnlinePlayerManager;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;

import java.nio.file.Path;

public interface Platform {
    KLogger getPlatformLogger();

    Path getPlatformDataPath();

    Path getPlatformConfigPath();

    OnlinePlayerManager getOnlinePlayerManager();

    LocalYggdrasilSessionService getLocalYggdrasilSessionService();

    String getPluginVersion();

    String getServerName();

    String getServerVersion();
}
