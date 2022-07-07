package fun.ksnb.multilogin.velocity.main;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fun.ksnb.multilogin.velocity.impl.VelocitySender;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import fun.ksnb.multilogin.velocity.injector.MultiInjTask;
import fun.ksnb.multilogin.velocity.logger.Slf4jLoggerBridge;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class MultiLoginVelocity implements IPlugin {
    private final Path dataDirectory;
    private final ProxyServer server;
    @Getter
    private final VelocityServer runServer;
    private final PluginLoader pluginLoader;
    private MultiCoreAPI multiCoreAPI;

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.runServer = new VelocityServer(this.server);
        this.dataDirectory = dataDirectory;
        LoggerProvider.setLogger(new Slf4jLoggerBridge(logger));
        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
            server.shutdown();
        }
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        try {
            multiCoreAPI = pluginLoader.getCoreObject();
            multiCoreAPI.load();
            new MultiInjTask(multiCoreAPI).run();
            CommandManager commandManager = server.getCommandManager();
            SimpleCommand commandHandler = new SimpleCommand() {
                @Override
                public void execute(Invocation invocation) {
                    String[] arguments = invocation.arguments();
                    String[] ns = new String[arguments.length + 1];
                    System.arraycopy(arguments, 0, ns, 1, arguments.length);
                    ns[0] = invocation.alias();
                    multiCoreAPI.getCommandHandler().execute(new VelocitySender(invocation.source()), ns);
                }

                @Override
                public List<String> suggest(Invocation invocation) {
                    String[] arguments = invocation.arguments();
                    String[] ns = new String[arguments.length + 1];
                    System.arraycopy(arguments, 0, ns, 1, arguments.length);
                    ns[0] = invocation.alias();
                    return multiCoreAPI.getCommandHandler().tabComplete(new VelocitySender(invocation.source()), ns);
                }
            };
            commandManager.register(commandManager.metaBuilder("multilogin").build(), commandHandler);
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            server.shutdown();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        try {
            multiCoreAPI.close();
            pluginLoader.close();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
        } finally {
            multiCoreAPI = null;
            server.shutdown();
        }
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public File getTempFolder() {
        return new File(getDataFolder(), "tmp");
    }

    @Override
    public String getVersion() {
        return JsonParser.parseReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/velocity-plugin.json")))
        ).getAsJsonObject().getAsJsonPrimitive("version").getAsString();
    }
}
