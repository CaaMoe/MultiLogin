package fun.ksnb.multilogin.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import lombok.Getter;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class MultiLoginVelocity  implements IPlugin {
    private final ProxyServer server;
    private final Logger logger;

    @Getter
    private static MultiLoginVelocity instance;
    private final File dataDirectory;

    @Getter
    private final MultiCore core = new MultiCore(this);
    private final IServer runServer;

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();
        runServer = new VelocityServer(server);
        instance = this;
    }

    @Override
    public void initService() {

    }

    @Override
    public void initOther() {

    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        if (level == LoggerLevel.ERROR) logger.error(message, throwable);
        else if (level == LoggerLevel.WARN) logger.warn(message, throwable);
        else if (level == LoggerLevel.INFO) logger.info(message, throwable);
        else if (level == LoggerLevel.DEBUG){}
        else logger.info(message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return runServer;
    }

    @Override
    public File getDataFolder() {
        return dataDirectory;
    }

    @Override
    public String getPluginVersion() {
        return server.getPluginManager().getPlugin("MultiLogin")
                .map(PluginContainer::getDescription)
                .map(PluginDescription::getVersion)
                .get().get();
    }

    public ProxyServer getServer() {
        return server;
    }
}
