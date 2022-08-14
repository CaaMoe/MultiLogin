package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;

public class BukkitInjector implements Injector {
    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        throw new RuntimeException("Unsupported Bukkit Server.");
    }
}
