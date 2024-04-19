package moe.caa.multilogin.api.plugin;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@ApiStatus.Internal
public interface IPlugin extends ExtendedService {
    @NotNull
    File getDataFolder();

    @NotNull
    File getTempFolder();

    @NotNull
    IScheduler getScheduler();
}
