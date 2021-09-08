package moe.caa.multilogin.core.library;

import moe.caa.multilogin.core.exception.LoadLibraryFailedException;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.IOUtil;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 依赖文件下载和处理程序
 */
public class LibrariesHandler {
    private final MultiCore core;
    private final File librariesFolder;

    /**
     * 构建这个依赖处理程序
     *
     * @param core            插件核心
     * @param librariesFolder 依赖存放文件夹
     */
    public LibrariesHandler(MultiCore core, File librariesFolder) {
        this.core = core;
        this.librariesFolder = librariesFolder;
    }

    /**
     * 加载依赖核心
     */
    public boolean init() {
        try {
            core.getLogger().log(LoggerLevel.INFO, "Loading libraries...");
            IOUtil.createNewFileOrFolder(librariesFolder, true);
            List<Library> needLoadLibraries = check();
            if (needLoadLibraries.size() == 0) return true;
            core.getLogger().log(LoggerLevel.INFO,
                    String.format("Need to load %d %s, namely %s.", needLoadLibraries.size(),
                            needLoadLibraries.size() == 1 ? "library" : "libraries", needLoadLibraries.stream().map(Library::getJarName).collect(Collectors.joining(", "))));
            List<File> needLoadFiles = download(needLoadLibraries);
            load(needLoadFiles);
            List<Library> failed = check();
            if (failed.size() == 0) {
                core.getLogger().log(LoggerLevel.INFO,
                        String.format("Loaded, %d failed.", failed.size()));
                return true;
            } else {
                LoadLibraryFailedException exception = new LoadLibraryFailedException(failed.stream().map(Library::getJarName).collect(Collectors.joining(", ")));
                core.getLogger().log(LoggerLevel.ERROR,
                        String.format("Load failed, %d failed, namely %s.", failed.size(), exception.getMessage()), exception);
            }
        } catch (Throwable t) {
            LoadLibraryFailedException exception = new LoadLibraryFailedException(t);
            core.getLogger().log(LoggerLevel.ERROR,
                    "Load failed.", exception);
        }
        return false;
    }

    /**
     * 加载 jar 文件到当前网络类加载器中
     *
     * @param needLoadFiles 文件列表
     * @throws Throwable 加载失败
     */
    private void load(List<File> needLoadFiles) throws Throwable {
        ClassLoader classLoader = this.getClass().getClassLoader();
        MethodHandle handle = ReflectUtil.getSuperLookup().unreflect(URLClassLoader.class.getDeclaredMethod("addURL", URL.class));
        for (File file : needLoadFiles) {
            handle.invoke(classLoader, file.toURI().toURL());
        }
    }

    /**
     * 返回所有需要加载的依赖集合
     *
     * @return 需要加载的依赖集合
     */
    private List<Library> check() {
        return Library.getLibraries().stream().filter(library -> !library.isLoaded()).collect(Collectors.toList());
    }

    /**
     * 下载依赖
     *
     * @param libraries 依赖列表
     * @return 下载后依赖的文件实例
     * @throws InterruptedException 线程同步异常
     */
    private List<File> download(List<Library> libraries) throws InterruptedException {
        List<File> ret = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(libraries.size());
        for (Library library : libraries) {
            File libraryFile = new File(librariesFolder, library.getJarName());

            if (libraryFile.exists()) {
                if (libraryFile.length() != 0) {
                    countDownLatch.countDown();
                    ret.add(libraryFile);
                    continue;
                }
                libraryFile.delete();
            }
            core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                try {
                    HttpUtil.downloadFile(library.getDownloadUrl(), libraryFile);
                    ret.add(libraryFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        return ret;
    }
}
