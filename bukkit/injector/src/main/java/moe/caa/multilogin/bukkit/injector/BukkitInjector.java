package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.logger.LoggerProvider;
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
        } catch (Throwable t0) {
            LoggerProvider.getLogger().error("The new injector cannot be used.", t0);
            try {
                String generalNHC = "moe.caa.multilogin.bukkit.injector.nms.old.NMSInjector";
                Injector injector = (Injector) Class.forName(generalNHC).getConstructor().newInstance();
                injector.inject(api);
                LoggerProvider.getLogger().warn("With older injectors, there may be some problems.");
            } catch (Throwable t1) {
                throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", t1);
            }
        }
    }
}
