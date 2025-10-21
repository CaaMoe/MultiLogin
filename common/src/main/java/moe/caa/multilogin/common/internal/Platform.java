package moe.caa.multilogin.common.internal;

import moe.caa.multilogin.common.internal.logger.KLogger;
import moe.caa.multilogin.common.internal.online.OnlinePlayerManager;

import java.nio.file.Path;

public interface Platform {
    KLogger getPlatformLogger();

    Path getPlatformDataPath();

    Path getPlatformConfigPath();

    OnlinePlayerManager getOnlinePlayerManager();
}
