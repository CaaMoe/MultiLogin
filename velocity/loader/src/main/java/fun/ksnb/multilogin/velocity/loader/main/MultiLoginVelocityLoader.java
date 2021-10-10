package fun.ksnb.multilogin.velocity.loader.main;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.loader.impl.ISectionLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.logging.Level;

public class MultiLoginVelocityLoader implements ISectionLoader {
    private final Logger logger;
    private final File dataDirectory;

    @SneakyThrows
    @Inject
    public MultiLoginVelocityLoader(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();

        MultiLoginCoreLoader coreLoader = new MultiLoginCoreLoader(this);
        boolean b = coreLoader.start("MultiLogin-Velocity.JarFile");
        if (!b) {
            server.shutdown();
            return;
        }

        Class<?> baseBungeePluginClass = Class.forName("fun.ksnb.multilogin.velocity.main.MultiLoginVelocity", true, coreLoader.getCurrentUrlClassLoader());
        Constructor<?> constructor = baseBungeePluginClass.getConstructor(ProxyServer.class, Logger.class, File.class);
        Object o = constructor.newInstance(server, logger, this.dataDirectory);
        server.getEventManager().register(this, o);
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        if (level == Level.SEVERE) getLogger().error(message, throwable);
        else if (level == Level.WARNING) getLogger().warn(message, throwable);
        else if (level == Level.INFO) getLogger().info(message, throwable);
        else getLogger().info(message, throwable);
    }

    @Override
    public File getDataFolder() {
        return dataDirectory;
    }
}
