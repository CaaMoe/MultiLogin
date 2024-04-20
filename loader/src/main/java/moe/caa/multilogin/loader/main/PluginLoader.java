package moe.caa.multilogin.loader.main;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.api.ExtendedService;
import moe.caa.multilogin.loader.api.IBootstrap;
import moe.caa.multilogin.loader.classloader.MultiCoreClassLoader;
import moe.caa.multilogin.loader.exception.LibraryLoadFailedException;
import moe.caa.multilogin.loader.library.Library;
import moe.caa.multilogin.loader.library.LibraryDownloadingTask;
import moe.caa.multilogin.loader.manager.LibraryDigestManager;
import moe.caa.multilogin.loader.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PluginLoader implements ExtendedService {
    public static final List<String> REPOSITORIES = List.of(
            "https://maven.aliyun.com/repository/public/",
            "https://repo.maven.apache.org/maven2/"
    );
    public final IBootstrap bootstrap;
    public final File librariesFolder;
    public final File temporaryRelocatedLibrariesFolder;
    public final MultiCoreClassLoader coreClassLoader;
    public final LibraryDigestManager libraryDigestManager;

    public PluginLoader(IBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.librariesFolder = new File(bootstrap.getDataFolder(), "libraries");
        this.temporaryRelocatedLibrariesFolder = new File(bootstrap.getTempFolder(), "relocatedLibraries");
        this.coreClassLoader = new MultiCoreClassLoader(PluginLoader.class.getClassLoader());
        this.libraryDigestManager = new LibraryDigestManager();
    }


    @Override
    public void disable() throws IOException {
        this.coreClassLoader.close();
    }

    @Override
    public void enable() throws Exception {
        deleteTemporaryFiles();

        FileUtil.createDirectories(
                bootstrap.getDataFolder(),
                bootstrap.getTempFolder(),

                librariesFolder,
                temporaryRelocatedLibrariesFolder
        );

        initLibraries();

        initPlatformAndCore();
    }

    private void initPlatformAndCore() {

    }

    private void initLibraries() throws Exception {
        LoggerProvider.logger.info("Initializing libraries...");

        for (Library library : Library.NECESSARY_LIBRARIES) {
            loadLibrary(library);
        }
    }

    private void loadLibrary(Library library) throws LibraryLoadFailedException {
        try {
            File file = library.getFile(librariesFolder);

            boolean needDownload = false;
            if (file.exists()) {
                LibraryDigestManager.ValidateResult validateResult = libraryDigestManager.validate(library, file);
                switch (validateResult) {
                    case NO_RECORD ->
                            throw new LibraryLoadFailedException("No digest recorded to library " + library.getDisplayName());
                    case NO_MATCHED -> {
                        needDownload = true;
                        LoggerProvider.logger.warn(
                                "Failed to validate digest value of file " + file.getAbsolutePath() + ", it will be re-downloaded."
                        );
                    }
                    case MATCHED -> {
                    }
                    default -> throw new LibraryLoadFailedException("Internal error: Unknown type " + validateResult);
                }
            }
            if (!file.exists() || needDownload) {
                try {
                    new LibraryDownloadingTask(library).download(file);
                } catch (IOException e) {
                    throw new LibraryLoadFailedException("Unable to download library " + library.getDisplayName(), e);
                }
            }

            LibraryDigestManager.ValidateResult validateResult = libraryDigestManager.validate(library, file);
            switch (validateResult) {
                case MATCHED -> {
                }
                case NO_RECORD ->
                        throw new LibraryLoadFailedException("No digest recorded to library " + library.getDisplayName());
                case NO_MATCHED ->
                        throw new LibraryLoadFailedException("The digest value of the file " + file.getAbsolutePath() + " just downloaded could not be verified.");
                default -> throw new LibraryLoadFailedException("Internal error: Unknown type " + validateResult);
            }

        } catch (LibraryLoadFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new LibraryLoadFailedException("Failed to handle library " + library.getDisplayName(), e);
        }

    }

    private void deleteTemporaryFiles() {
        try {
            FileUtil.deleteDirectory(bootstrap.getTempFolder().toPath());
        } catch (IOException e) {
            LoggerProvider.logger.error("An exception occurred while deleting the temporary directory.", e);
        }
    }
}
