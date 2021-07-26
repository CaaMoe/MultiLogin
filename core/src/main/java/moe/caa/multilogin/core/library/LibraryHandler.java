/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.library.LibraryHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.library;

import moe.caa.multilogin.core.exception.LoadLibraryFailedException;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FileUtil;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class LibraryHandler {
    private final MultiCore core;
    //    多线程并发同步器 不懂不许动！
    CountDownLatch countDownLatch;
    private boolean preLoaded = false;
    private Map<String, String> NEED_LIBRARIES = new LinkedHashMap<>();

    public LibraryHandler(MultiCore core) {
        this.core = core;
    }

    private String genUrl(String name) {
//     例子 方便生成URL   https://repo1.maven.org/maven2/com/zaxxer/HikariCP/4.0.2/HikariCP-4.0.2.jar
        String[] split = name.split(" ");
        StringBuilder sb = new StringBuilder("https://repo1.maven.org/maven2/");
        String[] packetSplit = split[0].split("\\.");
        for (String get : packetSplit) {
            sb.append(get);
            sb.append('/');
        }
        sb.append(split[1]);
        sb.append('/');
        sb.append(split[2]);
        sb.append('/');
        sb.append(split[1]);
        sb.append('-');
        sb.append(split[2]);
        sb.append(".jar");
        return sb.toString();
    }

    private String genJarName(String name) {
        String[] split = name.split(" ");
        return split[1] +
                '-' +
                split[2] +
                ".jar";
    }

    public void preInit() throws Throwable {
        preCheck();
        doDownload();
        preLoaded = true;
    }

    public void init() throws Throwable {
        check();
        doDownload();
//        回收对象
        NEED_LIBRARIES = null;
    }

    private void doDownload() throws Throwable {
        File libFolder = new File(MultiCore.getPlugin().getDataFolder(), "libraries");
        FileUtil.createNewFileOrFolder(libFolder, true);
        download(libFolder);
        load(libFolder);
        if (preLoaded) {
            check();
        } else {
//            NEED_LIBRARIES.clear();
            preCheck();
        }
        String args = NEED_LIBRARIES.keySet().stream().map(this::genJarName).collect(Collectors.joining(", "));
        if (args.length() == 0) return;
        if (preLoaded) {
            throw new LoadLibraryFailedException(LanguageKeys.LIBRARY_LOAD_FAILED.getMessage(core, args));
        } else {
            throw new LoadLibraryFailedException("Preload library load error " + args);
        }
    }

    private void load(File libFolder) throws Throwable {
//        ClassLoader classLoader = core.plugin.getClass().getClassLoader();
        //fabric需要我们原地起跳
        ClassLoader classLoader = this.getClass().getClassLoader();
        // super lookup 无视 accessible 属性
        MethodHandle handle = ReflectUtil.super_lookup.unreflect(ReflectUtil.getMethodWithParent(classLoader.getClass(), "addURL", false, URL.class));
        for (Map.Entry<String, String> library : NEED_LIBRARIES.entrySet()) {
            String jarName = genJarName(library.getKey());
            handle.invoke(classLoader, new File(libFolder, jarName).toURI().toURL());
            if (ReflectUtil.getClass(library.getValue()) != null) {
                if (preLoaded) {
                    core.getLogger().log(LoggerLevel.INFO, LanguageKeys.LIBRARY_LOADED.getMessage(core, jarName));
                } else {
                    core.getLogger().log(LoggerLevel.INFO, "Library loaded " + jarName);
                }
            } else {
                core.getLogger().log(LoggerLevel.INFO, "Library load fail " + jarName);
                throw new LoadLibraryFailedException();
            }
        }
    }

    private void download(File libFolder) throws InterruptedException {
        countDownLatch = new CountDownLatch(NEED_LIBRARIES.size());
        for (Map.Entry<String, String> library : NEED_LIBRARIES.entrySet()) {
            String jarName = genJarName(library.getKey());
            File file = new File(libFolder, jarName);
            if (file.exists()) {
                if (file.length() != 0) {
//                    排除0大小空文件
                    countDownLatch.countDown();
                    continue;
                } else {
                    file.delete();
                }
            }
            String url = genUrl(library.getKey());
            MultiCore.getPlugin().getSchedule().runTaskAsync(new DownloadThread(url, file, jarName));
        }
        countDownLatch.await();
    }

    private void check() {
        NEED_LIBRARIES.clear();

        if (ReflectUtil.getClass("org.slf4j.LoggerFactory") == null) {
            NEED_LIBRARIES.put("org.slf4j slf4j-api 1.7.31", "org.slf4j.LoggerFactory");
        }

        if (ReflectUtil.getClass("com.zaxxer.hikari.HikariDataSource") == null) {
            NEED_LIBRARIES.put("com.zaxxer HikariCP 4.0.3", "com.zaxxer.hikari.HikariDataSource");
        }

        if (ReflectUtil.getClass("com.mysql.cj.jdbc.MysqlDataSource") == null) {
            NEED_LIBRARIES.put("mysql mysql-connector-java 8.0.11", "com.mysql.cj.jdbc.MysqlDataSource");
        }

        if (ReflectUtil.getClass("org.h2.jdbcx.JdbcConnectionPool") == null) {
            NEED_LIBRARIES.put("com.h2database h2 1.4.200", "org.h2.jdbcx.JdbcConnectionPool");
        }

        if (ReflectUtil.getClass("com.google.gson.Gson") == null) {
            NEED_LIBRARIES.put("com.google.code.gson gson 2.8.7", "com.google.gson.Gson");
        }

        if (ReflectUtil.getClass("org.apache.logging.log4j.Level") == null) {
            NEED_LIBRARIES.put("org.apache.logging.log4j log4j-api 2.13.2", "org.apache.logging.log4j.Level");
        }

        if (ReflectUtil.getClass("org.apache.logging.log4j.core.LoggerContext") == null) {
            NEED_LIBRARIES.put("org.apache.logging.log4j log4j-core 2.13.2", "org.apache.logging.log4j.core.LoggerContext");
        }

        if (ReflectUtil.getClass("com.mojang.brigadier.CommandDispatcher") == null) {
            NEED_LIBRARIES.put("com.mojang brigadier 1.0.18", "com.mojang.brigadier.CommandDispatcher");
        }
    }

    private void preCheck() {
        NEED_LIBRARIES.clear();
        if (ReflectUtil.getClass("org.yaml.snakeyaml.DumperOptions") == null) {
            NEED_LIBRARIES.put("org.yaml snakeyaml 1.29", "org.yaml.snakeyaml.DumperOptions");
        }
    }

    //    下载线程
    private class DownloadThread implements Runnable {
        private final String url;
        private final File file;
        private final String jarName;

        private DownloadThread(String url, File file, String jarName) {
            this.url = url;
            this.file = file;
            this.jarName = jarName;
        }

        @Override
        public void run() {
            try {
                if (preLoaded) {
                    core.getLogger().log(LoggerLevel.INFO, LanguageKeys.LIBRARY_DOWNLOADING.getMessage(core, jarName));
                } else {
                    core.getLogger().log(LoggerLevel.INFO, "Library downloading " + jarName);
                }
                HttpUtil.downloadFile(url, file);
                if (preLoaded) {
                    core.getLogger().log(LoggerLevel.INFO, LanguageKeys.LIBRARY_DOWNLOADED.getMessage(core, file.getAbsolutePath()));
                } else {
                    core.getLogger().log(LoggerLevel.INFO, "Library downloaded:" + file.getAbsolutePath());
                }
            } catch (Exception exception) {
                if (preLoaded) {
                    core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.LIBRARY_DOWNLOAD_FAILED.getMessage(core, file.getAbsolutePath()), exception);
                } else {
                    core.getLogger().log(LoggerLevel.INFO, "Library downloaded:" + file.getAbsolutePath(), exception);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
