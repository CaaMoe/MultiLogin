package moe.caa.multilogin.api.plugin;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

@ApiStatus.Internal
public interface IPlugin {
    @NotNull
    File getDataFolder();

    @NotNull
    File getTempFolder();

    @NotNull
    ScheduledExecutorService getAsyncExecutor();
}
