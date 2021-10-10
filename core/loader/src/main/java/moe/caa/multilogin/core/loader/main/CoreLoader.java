package moe.caa.multilogin.core.loader.main;

import moe.caa.multilogin.core.loader.impl.ISectionLoader;
import moe.caa.multilogin.core.loader.libraries.Library;
import moe.caa.multilogin.core.loader.util.HttpUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * 核心部分加载器
 */
public class CoreLoader {
    private final AtomicInteger asyncThreadId = new AtomicInteger(0);

    private final ISectionLoader sectionLoader;
    private final File librariesFolder;


    /**
     * 构建这个核心加载器
     *
     * @param sectionLoader 部分加载器
     */
    public CoreLoader(ISectionLoader sectionLoader) {
        this.sectionLoader = sectionLoader;
        librariesFolder = new File(sectionLoader.getDataFolder(), "libraries");
    }

    /**
     * 开始下载和加载
     *
     * @param sectionJarFileName 部分需要加载的流名称
     */
    public void startLoader(String sectionJarFileName) {
        sectionLoader.loggerLog(Level.INFO, "Loading libraries...", null);
        generateLibrariesFolder();
    }

    /**
     * 开始下载需要加载的 Jar 包
     */
    private void startDownloadNeedLoader() throws InterruptedException {
        ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(5,
                r -> new Thread(r, "MultiLogin Async #" + asyncThreadId.incrementAndGet()));

        List<Library> needDownload = new ArrayList<>();
        for (Library library : Library.getJAR_RELOCATOR_LIBRARIES()) {
            File file = new File(librariesFolder, library.generateJarName());
            if (file.exists() && file.length() != 0) continue;
            needDownload.add(library);
        }

        for (Library library : Library.getLIBRARIES()) {
            File file = new File(librariesFolder, library.generateJarName());
            if (file.exists() && file.length() != 0) continue;
            needDownload.add(library);
        }

        CountDownLatch latch = new CountDownLatch(needDownload.size());
        asyncExecutor.execute(() -> {
            for (Library library : needDownload) {
                File output = new File(librariesFolder, library.generateJarName());
                try {
                    HttpUtil.downloadFile(library.generateDownloadUrl(), output);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("FAILED TO DOWNLOAD FILE %s. (%s)", library.generateJarName(), library.generateDownloadUrl()), e);
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();

    }

    @SuppressWarnings("all")
    public void generateLibrariesFolder() {
        if (!librariesFolder.exists()) {
            librariesFolder.mkdirs();
        }
    }
}
