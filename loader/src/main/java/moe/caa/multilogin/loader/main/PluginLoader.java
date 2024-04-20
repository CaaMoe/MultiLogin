package moe.caa.multilogin.loader.main;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.api.ExtendedService;
import moe.caa.multilogin.loader.api.IBootstrap;
import moe.caa.multilogin.loader.api.IPlatformCore;
import moe.caa.multilogin.loader.classloader.MultiCoreClassLoader;
import moe.caa.multilogin.loader.exception.LibraryLoadFailedException;
import moe.caa.multilogin.loader.handler.LibraryDigestHandler;
import moe.caa.multilogin.loader.handler.LibraryDownloadHandler;
import moe.caa.multilogin.loader.handler.LibraryRelocateHandler;
import moe.caa.multilogin.loader.library.Library;
import moe.caa.multilogin.loader.library.LibraryList;
import moe.caa.multilogin.loader.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader implements ExtendedService {
    public static final List<String> REPOSITORIES = List.of(
            "https://maven.aliyun.com/repository/public/",
            "https://repo.maven.apache.org/maven2/"
    );
    private final IBootstrap bootstrap;
    private final File librariesFolder;
    private final File temporaryRelocatedLibrariesFolder;
    public final MultiCoreClassLoader coreClassLoader;
    private final LibraryDigestHandler libraryDigestHandler;
    private final List<String> loadBeforeGroups = new ArrayList<>();
    private IPlatformCore<?> platformCore;

    public PluginLoader(IBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.librariesFolder = new File(bootstrap.getDataFolder(), "libraries");
        this.temporaryRelocatedLibrariesFolder = new File(bootstrap.getTempFolder(), "relocatedLibraries");
        this.coreClassLoader = new MultiCoreClassLoader(PluginLoader.class.getClassLoader());
        this.libraryDigestHandler = new LibraryDigestHandler();

        addLoadLibraryGroup("core");
    }

    public void addLoadLibraryGroup(String group) {
        loadBeforeGroups.add(group);
    }

    @Override
    public void disable() throws Exception {
        try {
            if (platformCore != null) platformCore.disable();
        } finally {
            this.coreClassLoader.close();
        }
    }

    @Override
    public void enable() throws Exception {
        deleteTemporaryFiles();

        IOUtil.createDirectories(
                bootstrap.getDataFolder(),
                bootstrap.getTempFolder(),

                librariesFolder,
                temporaryRelocatedLibrariesFolder
        );

        initLibraries();
        initPlatformAndCore();

        platformCore = bootstrap.generatePlatformCore(coreClassLoader);
        platformCore.enable();
    }

    private void initPlatformAndCore() throws LibraryLoadFailedException {
        File tmpMrf = new File(bootstrap.getTempFolder(), "multilogin");
        File tmpRelocateMrf = new File(temporaryRelocatedLibrariesFolder, "multilogin");
        String CORE_MODULE_FILE_NAME = "MultiLogin-Core";
        try {
            File core = new File(tmpMrf, CORE_MODULE_FILE_NAME);
            File coreRelocated = new File(tmpRelocateMrf, CORE_MODULE_FILE_NAME + "-Relocated.jar");
            IOUtil.saveCoverResource(getClass().getClassLoader(), CORE_MODULE_FILE_NAME, core);
            new LibraryRelocateHandler(core, coreRelocated).relocate();
            coreClassLoader.addURL(coreRelocated.toURI().toURL());
        } catch (Exception e) {
            throw new LibraryLoadFailedException("Failed to initialize core " + CORE_MODULE_FILE_NAME, e);
        }

        try {
            File platformCore = new File(tmpMrf, bootstrap.getPlatformCoreModuleFileName());
            File platformCoreRelocated = new File(tmpRelocateMrf, bootstrap.getPlatformCoreModuleFileName() + "-Relocated.jar");
            IOUtil.saveCoverResource(getClass().getClassLoader(), bootstrap.getPlatformCoreModuleFileName(), platformCore);
            new LibraryRelocateHandler(platformCore, platformCoreRelocated).relocate();
            coreClassLoader.addURL(platformCoreRelocated.toURI().toURL());
        } catch (Exception e) {
            throw new LibraryLoadFailedException("Failed to initialize platform core " + bootstrap.getPlatformCoreModuleFileName(), e);
        }
    }

    private void initLibraries() throws Exception {
        LoggerProvider.logger.info("Initializing libraries...");


        try {
            loadLibraries("relocate", false);
            LibraryRelocateHandler.init(coreClassLoader);
        } catch (Exception e) {
            throw new LibraryLoadFailedException("Failed to load relocate tools.", e);
        }

        for (String group : loadBeforeGroups) {
            loadLibraries(group, true);
        }
    }

    public void loadLibraries(String group) throws LibraryLoadFailedException {
        loadLibraries(group, true);
    }

    private void loadLibraries(String group, boolean relocate) throws LibraryLoadFailedException {
        List<Library> list = LibraryList.LIBRARY_GROUP.get(group);
        if (list == null) throw new LibraryLoadFailedException("Invalid library group " + group);
        for (Library library : list) {
            loadLibrary(library, relocate);
        }
    }

    private void loadLibrary(Library library, boolean relocate) throws LibraryLoadFailedException {
        try {
            File file = library.getFile(librariesFolder);

            boolean needDownload = false;
            if (file.exists()) {
                LibraryDigestHandler.ValidateResult validateResult = libraryDigestHandler.validate(library, file);
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
                    new LibraryDownloadHandler(library).download(file);
                } catch (IOException e) {
                    throw new LibraryLoadFailedException("Unable to download library " + library.getDisplayName(), e);
                }
            }

            LibraryDigestHandler.ValidateResult validateResult = libraryDigestHandler.validate(library, file);
            switch (validateResult) {
                case MATCHED -> {
                }
                case NO_RECORD ->
                        throw new LibraryLoadFailedException("No digest recorded to library " + library.getDisplayName());
                case NO_MATCHED ->
                        throw new LibraryLoadFailedException("The digest value of the file " + file.getAbsolutePath() + " just downloaded could not be verified.");
                default -> throw new LibraryLoadFailedException("Internal error: Unknown type " + validateResult);
            }
            if (relocate) {
                File relocated = library.getFileRelocated(temporaryRelocatedLibrariesFolder);
                try {
                    new LibraryRelocateHandler(file, relocated).relocate();
                } catch (Exception e) {
                    throw new LibraryLoadFailedException("Failed to relocate library " + library.getDisplayName(), e);
                }
                coreClassLoader.addURL(relocated.toURI().toURL());
            } else {
                coreClassLoader.addURL(file.toURI().toURL());
            }
        } catch (LibraryLoadFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new LibraryLoadFailedException("Failed to handle library " + library.getDisplayName(), e);
        }
    }

    private void deleteTemporaryFiles() {
        try {
            IOUtil.deleteDirectory(bootstrap.getTempFolder().toPath());
        } catch (IOException e) {
            LoggerProvider.logger.error("An exception occurred while deleting the temporary directory.", e);
        }
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return coreClassLoader.loadClass(className);
    }
}
