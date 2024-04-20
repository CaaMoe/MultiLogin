package moe.caa.multilogin.loader.api;

import moe.caa.multilogin.api.schedule.IScheduler;
import moe.caa.multilogin.loader.main.PluginLoader;

import java.io.File;

public interface IBootstrap {

    File getDataFolder();

    File getTempFolder();

    IScheduler getScheduler();

    String getPlatformCoreModuleFileName();

    IPlatformCore<?> generatePlatformCore(ClassLoader loader) throws Exception;

    PluginLoader getPluginLoader();
}
