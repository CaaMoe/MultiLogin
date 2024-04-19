package moe.caa.multilogin.loader.main;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.api.IBootstrap;
import moe.caa.multilogin.loader.library.Library;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PluginLoader {
    public static final Map<Library, String> DIGESTED_MAP;
    public final IBootstrap bootstrap;
    public final File librariesFolder;
    private final File temporaryRelocatedLibrariesFolder;

    static {
        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream(".digested");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(resourceAsStream));
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            Map<Library, String> tMap = new HashMap<>();
            lnr.lines().map(s -> s.split("="))
                    .forEach(ss -> tMap.put(Library.of(ss[0]), ss[1]));

            DIGESTED_MAP = Collections.unmodifiableMap(tMap);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to initialize internal data.", throwable);
        }
    }

    public PluginLoader(IBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.librariesFolder = new File(bootstrap.getDataFolder(), "libraries");
        this.temporaryRelocatedLibrariesFolder = new File(bootstrap.getTempFolder(), "relocatedLibraries");
    }

    public void init() throws IOException {
        LoggerProvider.logger.info("Initializing libraries...");

        deleteTemporaryFiles();

        createDirectories(
                bootstrap.getDataFolder(),
                bootstrap.getTempFolder(),

                librariesFolder,
                temporaryRelocatedLibrariesFolder
        );

        initLibraries();
    }

    private void initLibraries() {

    }


    private void createDirectories(File... files) throws IOException {
        for (File file : files) {
            if (!file.exists()) {
                Files.createDirectories(file.toPath());
            }
        }
    }

    private void deleteTemporaryFiles() {
        if (bootstrap.getTempFolder().exists()) {
            try {
                Files.walkFileTree(bootstrap.getTempFolder().toPath(), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LoggerProvider.logger.error("An exception occurred while deleting the temporary directory.", e);
            }
        }
    }

    public void close() {

    }
}
