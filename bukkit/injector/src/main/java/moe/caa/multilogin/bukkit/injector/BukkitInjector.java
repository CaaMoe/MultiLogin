package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;

/**
 * Bukkit 的注入程序
 */
public class BukkitInjector implements Injector {
    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        throw new RuntimeException("Unsupported Bukkit Server.");
    }
}
