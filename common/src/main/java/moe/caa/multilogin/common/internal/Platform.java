package moe.caa.multilogin.common.internal;

import moe.caa.multilogin.common.internal.logger.KLogger;

import java.nio.file.Path;

public interface Platform {
    KLogger getPlatformLogger();

    Path getPlatformDataPath();

    Path getPlatformConfigPath();
}
