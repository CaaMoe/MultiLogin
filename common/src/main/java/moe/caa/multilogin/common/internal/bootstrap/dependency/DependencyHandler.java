package moe.caa.multilogin.common.internal.bootstrap.dependency;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

class DependencyHandler implements Closeable {
    final MultiLoginDependencyHandler handler;
    final DependencyDownloader dependencyDownloader;
    final DependencyRelocator dependencyRelocator;

    public DependencyHandler(MultiLoginDependencyHandler handler) {
        this.handler = handler;

        this.dependencyDownloader = new DependencyDownloader(this);
        this.dependencyRelocator = new DependencyRelocator(this);
    }

    public void addDependencyRepository(String repositoryUrl) {
        if (repositoryUrl.endsWith("/")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 1);
        }
        dependencyDownloader.repositories.add(repositoryUrl);
    }

    public void addRelocation(String original, String relocated) {
        dependencyRelocator.relocations.put(original, relocated);
    }

    public void addExclude(String pattern) {
        dependencyRelocator.excludes.add(pattern);
    }

    public Path processDependency(Dependency dependency) throws Throwable {
        Path dependencyPath = dependencyDownloader.fetchDependency(dependency);
        return dependencyRelocator.relocate(dependency, dependencyPath);
    }

    @Override
    public void close() throws IOException {
        dependencyRelocator.close();
    }
}