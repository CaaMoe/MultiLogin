package moe.caa.multilogin.loader.api;

import moe.caa.multilogin.api.plugin.IScheduler;

import java.io.File;

public interface IBootstrap {
    File getDataFolder();

    File getTempFolder();

    IScheduler getScheduler();
}
