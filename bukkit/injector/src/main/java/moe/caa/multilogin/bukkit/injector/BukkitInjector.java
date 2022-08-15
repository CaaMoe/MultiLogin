package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;

import java.util.Locale;

/**
 * Bukkit 的注入程序
 */
public class BukkitInjector implements Injector {
    @Override
    public void inject(MultiCoreAPI api) {
        String nmsVersion = ((MultiLoginBukkit) api.getPlugin())
                .getServer().getClass().getName().split("\\.")[3];
        try {
            NMSHandlerEnum handlerEnum = NMSHandlerEnum.valueOf(nmsVersion.toLowerCase(Locale.ROOT));
            Injector injector = (Injector) Class.forName(handlerEnum.getNhc()).getConstructor().newInstance();
            injector.inject(api);
        } catch (Throwable throwable) {
            throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", throwable);
        }
    }
}
