package moe.caa.multilogin.core.loader.main;

import lombok.Getter;
import moe.caa.multilogin.core.loader.impl.ISectionLoader;
import moe.caa.multilogin.core.loader.libraries.Library;
import moe.caa.multilogin.core.loader.util.HttpUtil;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * 核心部分加载器
 */
public class MultiLoginCoreLoader {
    private final ISectionLoader sectionLoader;
    private final File librariesFolder;

    @Getter
    private URLClassLoader currentUrlClassLoader;

    /**
     * 构建这个核心加载器
     *
     * @param sectionLoader 部分加载器
     */
    public MultiLoginCoreLoader(ISectionLoader sectionLoader) {
        this.sectionLoader = sectionLoader;
        librariesFolder = new File(sectionLoader.getDataFolder(), "libraries");
    }

    /**
     * 开始加载这群依赖项目
     *
     * @param sectionJarFileName 部分需要加载的流名称
     */
    public boolean start(String sectionJarFileName){
        try {
            start0(sectionJarFileName);
            return true;
        } catch (Throwable e) {
            sectionLoader.loggerLog(Level.SEVERE, "A FATAL ERROR OCCURRED WHILE PROCESSING A DEPENDENCY", e);
            return false;
        }
    }

    /**
     * 开始加载这群依赖项目
     *
     * @param sectionJarFileName 部分需要加载的流名称
     */
    private void start0(String sectionJarFileName) throws Throwable {
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
            // 服务端有这个库的，略去
            if (library.isLoaded(getClass().getClassLoader())) continue;
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
        for (Library library : Library.getJAR_RELOCATOR_LIBRARIES()) {
            urls.add(new File(librariesFolder, library.generateJarName()).toURI().toURL());
        }
        currentUrlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());

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
            if(library.needRelocate()){
                File outFile = File.createTempFile("MultiLogin-", "-" + library.generateRemapJarName());
                outFile.deleteOnExit();
                Object o = jarRelocatorConstructor.invoke(file, outFile, library.getRelocateRules());
                jarRelocator_runMethod.invoke(o);
                urlList.add(outFile.toURI().toURL());
            } else {
                urlList.add(file.toURI().toURL());
            }
        }

        // 释放本体文件
        File fbt = File.createTempFile("MultiLogin-", "-" + sectionJarFileName + ".jar");
        fbt.deleteOnExit();

        try (InputStream input = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(sectionJarFileName)
                , "sectionJarFileName is null.");
                FileOutputStream output = new FileOutputStream(fbt)){
            byte[] buff =new byte[1024];
            int b ;
            while ((b = input.read(buff)) != -1) {
                output.write(buff,0,b);
            }
            output.flush();
        }

        urlList.add(fbt.toURI().toURL());

        // 释放
        currentUrlClassLoader.close();

        // 加载
        currentUrlClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), getClass().getClassLoader());
    }

    /**
     * 注销
     */
    public void close() {
        try {
            currentUrlClassLoader.close();
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
                    HttpUtil.downloadFile(library.generateDownloadUrl(), output);
                } catch (Throwable e) {
                    downloadFailed.set(true);
                    throw new RuntimeException(String.format("FAILED TO DOWNLOAD FILE %s. (%s)", library.generateJarName(), library.generateDownloadUrl()), e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        asyncExecutor.shutdown();
        if (downloadFailed.get()) {
            throw new RuntimeException("ONE OR MORE MISSING FILES FAILED TO DOWNLOAD.");
        }
    }

    /**
     * 生成放置依赖的文件夹
     */
    @SuppressWarnings("all")
    private void generateLibrariesFolder() {
        if (!librariesFolder.exists()) {
            librariesFolder.mkdirs();
        }
    }
}
