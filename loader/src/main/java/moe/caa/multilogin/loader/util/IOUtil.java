package moe.caa.multilogin.loader.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class IOUtil {
    public static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
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
        }
    }


    public static void createDirectories(File... files) throws IOException {
        for (File file : files) {
            if (!file.exists()) {
                Files.createDirectories(file.toPath());
            }
        }
    }

    public static void saveCoverResource(ClassLoader classLoader, String resource, File output) throws IOException {
        if (!output.getParentFile().exists()) {
            Files.createDirectories(output.getParentFile().toPath());
        }

        try (InputStream resourceAsStream = Objects.requireNonNull(classLoader.getResourceAsStream(resource),
                "Failed processing resource " + resource
        )) {
            try (OutputStream outputStream = Files.newOutputStream(output.toPath())) {
                resourceAsStream.transferTo(outputStream);
            }
        }
    }
}
