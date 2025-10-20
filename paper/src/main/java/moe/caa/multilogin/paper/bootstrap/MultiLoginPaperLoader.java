package moe.caa.multilogin.paper.bootstrap;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import moe.caa.multilogin.common.internal.bootstrap.dependency.MultiLoginDependencyHandler;
import moe.caa.multilogin.common.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;

public class MultiLoginPaperLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
        KLogger logger = new KLogger(pluginClasspathBuilder.getContext().getLogger());
        logger.checkLogAsDebugFlag(pluginClasspathBuilder.getContext().getDataDirectory());
        long startTime = System.currentTimeMillis();
        logger.info("Initializing MultiLogin dependencies...");
        try (var multiLoginDependencyHandler = new MultiLoginDependencyHandler(
                logger,
                pluginClasspathBuilder.getContext().getDataDirectory(),
                pluginClasspathBuilder.getContext().getDataDirectory()
        )) {
            multiLoginDependencyHandler.initDependencies(path -> pluginClasspathBuilder.addLibrary(new JarLibrary(path)));
            long endTime = System.currentTimeMillis();
            logger.info("MultiLogin dependencies initialized in " + (endTime - startTime) + "ms.");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize MultiLogin dependencies.", e);
        }
    }
}
