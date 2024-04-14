package moe.caa.multilogin.api;

import moe.caa.multilogin.api.logging.IMultiloginLogger;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;

import java.io.File;

public interface IMultiloginPlatform {
    IMultiloginLogger getLogger();

    CommandManager<Audience> getCommandManager(ExecutionCoordinator<Audience> executionCoordinator);

    File getDataFolder();

    boolean isOnlineMode();

    boolean isProfileForwarding();
}
