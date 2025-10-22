package moe.caa.multilogin.common.internal.bootstrap.dependency;

import moe.caa.multilogin.common.internal.logger.KLogger;
import moe.caa.multilogin.common.internal.util.IOUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiLoginDependencyHandler implements Closeable {
    final KLogger logger;
    final Path configDirectory;
    final Path dataDirectory;
    final Path dependenciesDirectory;
    final DependencyHandler dependencyHandler;

    final List<String> defaultOutsideDependencies = List.of(
            "com.h2database:h2:2.4.240"
    );
    final List<String> defaultOutsideRepositories = List.of();
    final List<String> defaultOutsideRelocations = List.of();

    public MultiLoginDependencyHandler(KLogger logger, Path configDirectory, Path dataDirectory) {
        this.logger = logger;
        this.configDirectory = configDirectory;
        this.dataDirectory = dataDirectory;
        this.dependenciesDirectory = dataDirectory.resolve("libraries");
        this.dependencyHandler = new DependencyHandler(this);
    }

    public void initDependencies(Consumer<Path> dependencyConsumer) throws Throwable {
        if (!Files.exists(configDirectory)) {
            Files.createDirectories(configDirectory);
        }

        Path outsideRepositories = configDirectory.resolve("repositories.txt");
        Path outsideRelocations = configDirectory.resolve("relocations.txt");
        Path outsideDependencies = configDirectory.resolve("dependencies.txt");

        if (!Files.exists(outsideRepositories)) {
            Files.writeString(outsideRepositories, """
                    # You can add your own dependency repositories here.
                    # One repository per line.
                    # Lines starting with # are ignored.
                    # Example:
                    # https://repo.maven.apache.org/maven2/
                    """);

            for (String defaultOutsideRepository : defaultOutsideRepositories) {
                Files.writeString(outsideRepositories, defaultOutsideRepository + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        }

        if (!Files.exists(outsideRelocations)) {
            Files.writeString(outsideRelocations, """
                    # You can add your own dependency relocations here.
                    # One relocation per line, format: <original> <relocated>
                    # Lines starting with # are ignored.
                    # Example:
                    # com.google.guava com.yourorg.shadowed.com.google.guava
                    """);

            for (String defaultOutsideDependency : defaultOutsideRelocations) {
                Files.writeString(outsideRelocations, defaultOutsideDependency + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        }

        if (!Files.exists(outsideDependencies)) {
            Files.writeString(outsideDependencies, """
                    # You can add your own dependencies here.
                    # One dependency per line, format: <groupId>:<artifactId>:<version>
                    # Lines starting with # are ignored.
                    # Example:
                    # com.google.guava:guava:32.1.2-jre
                    """);

            for (String defaultOutsideDependency : defaultOutsideDependencies) {
                Files.writeString(outsideDependencies, defaultOutsideDependency + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        }

        Predicate<String> filterCommentFunction = s -> !s.isEmpty() && !s.startsWith("#");

        // repository
        List<String> repositories = Arrays.stream(new String(
                        Objects.requireNonNull(IOUtil.readNestResource("repositories.txt"), "Failed to read repositories.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(filterCommentFunction)
                .toList();
        for (String repository : repositories) {
            dependencyHandler.addDependencyRepository(repository);
            logger.debug("Added dependency repository: " + repository);
        }
        List<String> outsideRepositoriesList = Files.readAllLines(outsideRepositories).stream()
                .map(String::trim)
                .filter(filterCommentFunction)
                .toList();
        for (String outsideRepository : outsideRepositoriesList) {
            dependencyHandler.addDependencyRepository(outsideRepository);
            logger.info("Added outside dependency repository: " + outsideRepository);
        }

        // relocation
        List<String[]> relocations = Arrays.stream(new String(
                        Objects.requireNonNull(IOUtil.readNestResource("relocations.txt"), "Failed to read relocations.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(filterCommentFunction)
                .map(s -> s.split("\\s+"))
                .toList();
        for (String[] relocation : relocations) {
            if (relocation.length == 1) {
                dependencyHandler.addExclude(relocation[0]);
                logger.debug("Added dependency relocate exclusion: " + relocation[0]);
            } else if (relocation.length == 2) {
                String original = relocation[0];
                String relocated = relocation[1];
                dependencyHandler.addRelocation(original, relocated);
                logger.debug("Added dependency relocation: " + original + " -> " + relocated);
            } else {
                throw new IllegalArgumentException("Invalid relocation entry: " + Arrays.toString(relocation));
            }
        }
        List<String[]> outsideRelocationsList = Files.readAllLines(outsideRelocations).stream()
                .map(String::trim)
                .filter(filterCommentFunction)
                .map(s -> s.split("\\s+"))
                .toList();
        for (String[] outsideRelocation : outsideRelocationsList) {
            if (outsideRelocation.length == 1) {
                dependencyHandler.addExclude(outsideRelocation[0]);
                logger.info("Added outside dependency relocate exclusion: " + outsideRelocation[0]);
            } else if (outsideRelocation.length == 2) {
                String original = outsideRelocation[0];
                String relocated = outsideRelocation[1];
                dependencyHandler.addRelocation(original, relocated);
                logger.info("Added outside dependency relocation: " + original + " -> " + relocated);
            } else {
                throw new IllegalArgumentException("Invalid relocation entry: " + Arrays.toString(outsideRelocation));
            }
        }

        // dependency
        List<String> dependencies = Arrays.stream(new String(
                        Objects.requireNonNull(IOUtil.readNestResource("dependencies.txt"), "Failed to read dependencies.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(filterCommentFunction)
                .toList();

        for (String dependencyStr : dependencies) {
            Dependency dependency = Dependency.ofString(dependencyStr);
            dependencyConsumer.accept(dependencyHandler.processDependency(dependency));
            logger.debug("Loaded dependency: " + dependency);
        }

        for (String dependencyStr : dependencies) {
            Dependency dependency = Dependency.ofString(dependencyStr);
            dependencyConsumer.accept(dependencyHandler.processDependency(dependency));
            logger.debug("Loaded dependency: " + dependency);
        }
        List<String> outsideDependenciesList = Files.readAllLines(outsideDependencies).stream()
                .map(String::trim)
                .filter(filterCommentFunction)
                .toList();

        for (String dependencyStr : outsideDependenciesList) {
            Dependency dependency = Dependency.ofString(dependencyStr);
            dependencyConsumer.accept(dependencyHandler.processDependency(dependency));
            logger.info("Loaded outside dependency: " + dependency);
        }
    }

    @Override
    public void close() throws IOException {
        dependencyHandler.close();
    }
}
