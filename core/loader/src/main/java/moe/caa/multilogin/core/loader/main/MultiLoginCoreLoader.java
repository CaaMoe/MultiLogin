package moe.caa.multilogin.core.loader.main;

import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.libraries.Library;
import moe.caa.multilogin.core.loader.util.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * 插件加载器
 */
public class MultiLoginCoreLoader {
    private final IPluginLoader sectionLoader;
    private final File librariesFolder;
    private final File tempLibrariesFolder;
    private final LoaderType loaderType;
    private URLClassLoader currentUrlClassLoader;

    /**
     * 构建这个核心加载器
     *
     * @param sectionLoader 部分加载器
     * @param loaderType 加载方式
     */
    public MultiLoginCoreLoader(IPluginLoader sectionLoader, LoaderType loaderType) throws IOException {
        this.loaderType = loaderType;
        this.sectionLoader = sectionLoader;
        librariesFolder = new File(sectionLoader.getDataFolder(), "libraries");
        tempLibrariesFolder = new File(sectionLoader.getDataFolder(), "temp");
        Files.deleteIfExists(tempLibrariesFolder.toPath());
    }

    /**
     * 构建这个核心加载器
     *
     * @param sectionLoader 部分加载器
     */
    public MultiLoginCoreLoader(IPluginLoader sectionLoader) throws IOException {
        this(sectionLoader, LoaderType.NEST_JAR);
    }

    /**
     * 开始加载这群依赖项目
     */
    public boolean startLoading() {
        try {
            startLoading0();
            return false;
        } catch (Throwable e) {
            sectionLoader.loggerLog(Level.SEVERE, "A FATAL ERROR OCCURRED WHILE PROCESSING A DEPENDENCY", e);
            sectionLoader.loggerLog(Level.SEVERE, String.format("PLEASE DELETE '%s' AND TRY AGAIN.", librariesFolder.getAbsolutePath()), null);
            sectionLoader.shutdown();
            return true;
        }
    }

    /**
     * 开始加载这群依赖项目
     */
    private void startLoading0() throws Throwable {
        sectionLoader.loggerLog(Level.INFO, "Loading libraries...", null);
        // 生成放置依赖的文件夹
        generateLibrariesFolder();
        // 需要加载的依赖
        List<Library> needLoad = new ArrayList<>();
        // 需要下载的依赖
        List<Library> needDownload = new ArrayList<>();
        // 这里的依赖不包含重定向工具库
        for (Library library : Library.getLIBRARIES()) {
            // 服务端有这个库的，略去
            if (library.isLoaded(getClass().getClassLoader())) continue;
            // 服务端没有的，加入需要load列表中去
            needLoad.add(library);
            // 存在且大小不为 0kb 的 Jar 包，略去
            File loadFile = new File(librariesFolder, library.generateJarName());
            if (loadFile.exists() && loadFile.length() != 0) continue;
            // 不存在依赖本体的，放置到需要下载的集合中去
            needDownload.add(library);
        }

        // 这里的依赖只包含重定向工具库，运行时不需要加载他们
        for (Library library : Library.getJAR_RELOCATOR_LIBRARIES()) {
            // 10/23/21 禁止略去
            // if (library.isLoaded(getClass().getClassLoader())) continue;
            // 这里不需要也不允许加入到load列表中去
            // needLoad.add(library);
            // 存在且大小不为 0kb 的 Jar 包，略去
            File loadFile = new File(librariesFolder, library.generateJarName());
            if (loadFile.exists() && loadFile.length() != 0) continue;
            // 不存在依赖本体的，放置到需要下载的集合中去
            needDownload.add(library);
        }
        // 有下载请求的，打印日志
        if (needDownload.size() != 0) {
            sectionLoader.loggerLog(Level.INFO, "Downloading missing files, this will take a while...", null);
        }
        // 开始下载
        downloadLibraries(needDownload);

        // 这里使用 URLClassLoader 加载重定向工具库包
        List<URL> urls = new ArrayList<>();
        // 添加包名作为高优先级
        Set<String> packageName = new HashSet<>();
        for (Library library : Library.getJAR_RELOCATOR_LIBRARIES()) {
            packageName.add(library.getStartsPackName());
            urls.add(new File(librariesFolder, library.generateJarName()).toURI().toURL());
        }
        currentUrlClassLoader = new PriorURLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader(), packageName);

        // 加载反射值
        Class<?> jarRelocatorClass = Class.forName("me.lucko.jarrelocator.JarRelocator", true, currentUrlClassLoader);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle jarRelocatorConstructor = lookup.unreflectConstructor(jarRelocatorClass.getConstructor(File.class, File.class, Map.class));
        MethodHandle jarRelocator_runMethod = lookup.unreflect(jarRelocatorClass.getMethod("run"));

        // 这里存放要加载的文件
        List<URL> urlList = new ArrayList<>();
        // 这里的 library 都会有对应的依赖文件
        for (Library library : needLoad) {
            File file = new File(librariesFolder, library.generateJarName());
            if (library.needRelocate()) {
                File outFile = File.createTempFile("MultiLogin-", "-" + library.generateRemapJarName(), tempLibrariesFolder);
                outFile.deleteOnExit();
                Object o = jarRelocatorConstructor.invoke(file, outFile, library.getRelocateRules());
                jarRelocator_runMethod.invoke(o);
                urlList.add(outFile.toURI().toURL());
            } else {
                urlList.add(file.toURI().toURL());
            }
        }

        currentUrlClassLoader.close();

        if(loaderType == LoaderType.NEST_JAR){
            // 释放本体文件
            File fbt = File.createTempFile("MultiLogin-", "-" + sectionLoader.getSectionJarFileName() + ".jar", tempLibrariesFolder);
            fbt.deleteOnExit();

            try (InputStream input = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(sectionLoader.getSectionJarFileName())
                    , "sectionJarFileName is null.");
                 FileOutputStream output = new FileOutputStream(fbt)) {
                byte[] buff = new byte[1024];
                int b;
                while ((b = input.read(buff)) != -1) {
                    output.write(buff, 0, b);
                }
                output.flush();
            }

            urlList.add(fbt.toURI().toURL());

            currentUrlClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), getClass().getClassLoader());
        } else {
            // TODO: 2021/10/25 FORCE THE URLClassLoader
        }
    }

    /**
     * 获得插件引导类实例
     *
     * @param bootStrapClassName 插件引导类名称
     * @param argTypes           插件引导类构造方法参数列表
     * @param args               插件引导类构造方法参数
     * @return 插件引导类
     */
    public BasePluginBootstrap loadBootstrap(String bootStrapClassName, Class<?>[] argTypes, Object[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(loaderType == LoaderType.NEST_JAR){
            Class<?> bootstrapClass = Class.forName(bootStrapClassName, true, currentUrlClassLoader);
            Constructor<?> constructor = bootstrapClass.getConstructor(argTypes);
            return (BasePluginBootstrap) constructor.newInstance(args);
        }
        Class<?> bootstrapClass = Class.forName(bootStrapClassName);
        Constructor<?> constructor = bootstrapClass.getConstructor(argTypes);
        return (BasePluginBootstrap) constructor.newInstance(args);
    }

    /**
     * 注销资源
     */
    public void close() {
        try {
            if (currentUrlClassLoader != null)
                currentUrlClassLoader.close();
        } catch (IOException ignored) {
        }

        try {
            Files.deleteIfExists(tempLibrariesFolder.toPath());
        } catch (IOException ignored) {
        }
    }

    /**
     * 下载依赖库
     *
     * @param downloads 需要下载的集合
     * @throws InterruptedException 下载失败
     */
    private void downloadLibraries(List<Library> downloads) throws InterruptedException {
        if (downloads.size() == 0) return;
        AtomicInteger asyncThreadId = new AtomicInteger(0);
        ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(5,
                r -> new Thread(r, "MultiLogin Async #" + asyncThreadId.incrementAndGet()));
        // 存放下载失败的标志
        AtomicBoolean downloadFailed = new AtomicBoolean(false);
        // 信号量同步
        CountDownLatch latch = new CountDownLatch(downloads.size());
        for (Library library : downloads) {
            asyncExecutor.execute(() -> {
                File output = new File(librariesFolder, library.generateJarName());
                try {
                    sectionLoader.loggerLog(Level.INFO, String.format("Downloading: %s", library.generateDownloadUrl()), null);
                    HttpUtil.downloadFile(library.generateDownloadUrl(), output);
                } catch (Throwable e) {
                    downloadFailed.set(true);
                    sectionLoader.loggerLog(Level.SEVERE, String.format("FAILED TO DOWNLOAD FILE %s. (%s)", library.generateJarName(), library.generateDownloadUrl()), e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        asyncExecutor.shutdown();
        if (downloadFailed.get()) {
            RuntimeException exception = new RuntimeException("ONE OR MORE MISSING FILES FAILED TO DOWNLOAD.");
            sectionLoader.loggerLog(Level.SEVERE, "ONE OR MORE MISSING FILES FAILED TO DOWNLOAD.", exception);
            throw exception;
        }
    }

    /**
     * 生成放置依赖的文件夹
     */
    private void generateLibrariesFolder() {
        if (!librariesFolder.exists()) {
            librariesFolder.mkdirs();
        }
        if(!tempLibrariesFolder.exists()){
            tempLibrariesFolder.mkdirs();
        }
    }
}
