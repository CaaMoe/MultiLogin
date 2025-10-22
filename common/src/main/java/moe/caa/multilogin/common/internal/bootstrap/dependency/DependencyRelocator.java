package moe.caa.multilogin.common.internal.bootstrap.dependency;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class DependencyRelocator implements Closeable {
    private final List<Dependency> relocateToolDependencies = List.of(
            new Dependency("org.ow2.asm", "asm", "9.2"),
            new Dependency("org.ow2.asm", "asm-commons", "9.2"),
            new Dependency("me.lucko", "jar-relocator", "1.7")
    );
    private final DependencyHandler dependencyHandler;
    Map<String, String> relocations = new ConcurrentHashMap<>();
    List<String> excludes = new CopyOnWriteArrayList<>();
    private volatile RelocateTool relocateTool;

    DependencyRelocator(DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    Path relocate(Dependency dependency, Path dependencyJarPath) throws Throwable {
        RelocateTool tool = relocateTool;
        if (tool == null) {
            synchronized (this) {
                if (relocateTool == null) {
                    relocateTool = new RelocateTool();
                }
            }
            return relocate(dependency, dependencyJarPath);
        }
        return tool.relocate(dependency, dependencyJarPath);
    }

    @Override
    public void close() throws IOException {
        RelocateTool tool = relocateTool;
        if (tool != null) {
            tool.close();
        }
    }

    private class RelocateTool implements Closeable {
        private final URLClassLoader urlClassLoader;
        private final MethodHandle jarRelocatorConstructor;
        private final MethodHandle jarRelocatorRunMethod;

        private final MethodHandle relocationConstructor;

        private RelocateTool() throws Throwable {
            long startTimeMills = System.currentTimeMillis();
            dependencyHandler.handler.logger.debug("Initializing dependency relocator tool...");

            List<URL> dependencyURLs = new CopyOnWriteArrayList<>();

            for (Dependency dependency : relocateToolDependencies) {
                dependencyURLs.add(dependencyHandler.dependencyDownloader.fetchDependency(dependency).toUri().toURL());
                dependencyHandler.handler.logger.debug("Loaded relocate dependency: " + dependency);
            }

            urlClassLoader = new URLClassLoader(dependencyURLs.toArray(new URL[0]), DependencyRelocator.class.getClassLoader());

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> jarRelocatorClass = urlClassLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
            jarRelocatorConstructor = lookup.unreflectConstructor(jarRelocatorClass.getConstructor(File.class, File.class, Collection.class));
            jarRelocatorRunMethod = lookup.unreflect(jarRelocatorClass.getMethod("run"));

            Class<?> relocationClass = urlClassLoader.loadClass("me.lucko.jarrelocator.Relocation");
            relocationConstructor = lookup.unreflectConstructor(relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class));

            dependencyHandler.handler.logger.debug("Initialized dependency relocator tool, took " +
                    (System.currentTimeMillis() - startTimeMills) + " ms");
        }

        Path relocate(Dependency dependency, Path dependencyJarPath) throws Throwable {
            long startTimeMills = System.currentTimeMillis();
            Path relocatedDependencyJarPath = dependencyHandler.handler.dependenciesDirectory.resolve(dependency.getRelocatedJarPath());
            if (!Files.exists(relocatedDependencyJarPath.getParent())) {
                Files.createDirectories(relocatedDependencyJarPath.getParent());
            }

            List<Object> relocationRules = new ArrayList<>();
            for (Map.Entry<String, String> entry : relocations.entrySet()) {
                String pattern = entry.getKey();
                String destination = entry.getValue();
                Collection<String> excludes = DependencyRelocator.this.excludes;
                Object relocationRule = relocationConstructor.invoke(pattern, destination, null, excludes);
                relocationRules.add(relocationRule);
            }

            Object jarRelocatorObject = jarRelocatorConstructor.invoke(
                    dependencyJarPath.toFile(),
                    relocatedDependencyJarPath.toFile(),
                    relocationRules
            );

            jarRelocatorRunMethod.invoke(jarRelocatorObject);

            dependencyHandler.handler.logger.debug(
                    "Relocated dependency " + dependencyJarPath.toAbsolutePath() +
                            " to " + relocatedDependencyJarPath.toAbsolutePath() +
                            ", took " + (System.currentTimeMillis() - startTimeMills) + " ms");
            return relocatedDependencyJarPath;
        }

        @Override
        public void close() throws IOException {
            urlClassLoader.close();
        }
    }
}