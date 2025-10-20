package moe.caa.multilogin.common.internal.bootstrap.dependency;


import moe.caa.multilogin.common.internal.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class DependencyDownloader {
    final DependencyHandler dependencyHandler;
    final Set<String> repositories = new LinkedHashSet<>();

    DependencyDownloader(DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    protected Path fetchDependency(Dependency dependency) throws IOException, NoSuchAlgorithmException {
        String expectSha1Hex = new String(downloadFromAnyURL(dependency + "'s sha1", repositories.stream().map(dependency::generateJarDownloadSha1URL).toList()), StandardCharsets.UTF_8);
        Path savePath = dependencyHandler.handler.dependenciesDirectory.resolve(dependency.getJarPath());
        if (Files.exists(savePath)) {
            String activeSha1Hex = IOUtil.bytesToHex(MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(savePath)));
            if (activeSha1Hex.equals(expectSha1Hex)) {
                return savePath;
            }
            dependencyHandler.handler.logger.warn("Dependency " + dependency + " already exists in " + savePath.toAbsolutePath() +
                    " but SHA1 mismatch: expected " + expectSha1Hex + ", got " + activeSha1Hex + ". Redownloading.");
        }
        byte[] jarBytes = downloadFromAnyURL(dependency + "'s jar", repositories.stream().map(dependency::generateJarDownloadURL).toList());
        if (!Files.exists(savePath.getParent())) {
            Files.createDirectories(savePath.getParent());
        }
        Files.write(savePath, jarBytes);

        String activeSha1Hex = IOUtil.bytesToHex(MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(savePath)));
        if (!activeSha1Hex.equals(expectSha1Hex)) {
            throw new IOException("Downloaded dependency " + dependency + " in " + savePath.toAbsolutePath() + " but SHA1 mismatch: expected " + expectSha1Hex + ", got " + activeSha1Hex);
        }
        return savePath;
    }

    private byte[] downloadFromAnyURL(String name, List<String> urls) throws IOException {
        IOException exception = null;
        for (String url : urls) {
            try {
                return download(url);
            } catch (Exception e) {
                if (exception == null) {
                    exception = new IOException("Failed to download " + name + " from any URLs: " + urls);
                }
                exception.addSuppressed(e);
            }
        }
        if (exception == null) {
            exception = new IOException("Failed to download " + name + " from any URLs: " + urls);
        }
        throw exception;
    }

    private byte[] download(String url) throws IOException {
        dependencyHandler.handler.logger.debug("Downloading from " + url);
        long currentTimeMills = System.currentTimeMillis();
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            byte[] bytes = inputStream.readAllBytes();
            dependencyHandler.handler.logger.debug("Downloaded " + bytes.length + " bytes from " + url + ", took " + (System.currentTimeMillis() - currentTimeMills) + " ms.");
            return bytes;
        }
    }
}