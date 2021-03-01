package moe.caa.multilogin.core.lib;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.http.HttpDownload;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class LibChecker {
    //    下载池子
    ExecutorService executor = Executors.newCachedThreadPool();
    //    下载任务列表
    Map<String, FutureTask<Boolean>> downloadTasks = new HashMap<>();
    List<String> toLoad = new ArrayList<>();
    File libFolder;
    boolean downloadFail = false;
    URLClassLoader urlClassLoader;
    MethodHandle addUrlMH;
    boolean loadFail = false;
    Method addUrlMethod;

    public LibChecker(File pluginDataFolder) {
        libFolder = new File(pluginDataFolder, "libs");
        if (!libFolder.exists()) libFolder.mkdirs();
    }

    public boolean check() {
        checkClass();
        MultiCore.info("开始加载库");
        startDownload();
        if (downloadFail) return false;
        load();
        executor.shutdown();
        return !loadFail;
    }

    private void checkClass() {
//        检测HikariCP
        if (getClass("com.zaxxer.hikari.HikariDataSource") == null) {
            toLoad.add("com.zaxxer HikariCP 4.0.2");
        }
//        检测SLF4J
        if (getClass("org.slf4j.LoggerFactory") == null) {
            toLoad.add("org.slf4j slf4j-api 1.8.0-beta4");
        }
//        检测H2
        if (getClass("org.h2.jdbcx.JdbcConnectionPool") == null) {
            toLoad.add("com.h2database h2 1.4.200");
        }
//        检测JDBC
        if (getClass("com.mysql.jdbc.Driver") == null) {
            toLoad.add("mysql mysql-connector-java 5.1.48");
        }
    }

    private Class getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {

        }
        return null;
    }

    private void startDownload() {
        Map<String, File> toDownload = new HashMap<>();
//        确定要下载的目标
        for (String get : toLoad) {
            File toCheck = new File(libFolder, genJarName(get));
            if (toCheck.exists()) continue;
            toDownload.put(get, toCheck);
        }
        if (toDownload.isEmpty()) return;
        for (Map.Entry<String, File> downLoadEntry : toDownload.entrySet()) {
//            创建下载任务
            download(downLoadEntry.getKey(), downLoadEntry.getValue());
        }
        while (!downloadTasks.isEmpty()) {
            try {
                Iterator<FutureTask<Boolean>> it = downloadTasks.values().iterator();
                while (it.hasNext()) {
                    Future<Boolean> task = it.next();
                    if (!task.isDone()) continue;
                    if (!task.get()) downloadFail = true;
                    it.remove();
                }
            } catch (Exception e) {
                downloadFail = true;
                e.printStackTrace();
            }
        }
    }

    public void download(String name, File downloadFile) {
        MultiCore.info("开始下载 " + name);
        String url = genUrl(name);
        HttpDownload download = new HttpDownload(url, downloadFile);
        FutureTask<Boolean> task = new FutureTask<>(download);
        executor.submit(task);
        downloadTasks.put(name, task);
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
        StringBuilder sb = new StringBuilder();
        sb.append(split[1]);
        sb.append('-');
        sb.append(split[2]);
        sb.append(".jar");
        return sb.toString();
    }

    private void load() {
        if (toLoad.isEmpty()) return;
        try {
            initLoader();
            for (String name : toLoad) {
                load(name);
            }
        } catch (Throwable throwable) {
            loadFail = true;
            throwable.printStackTrace();
        }
        addUrlMethod.setAccessible(false);
    }

    private void initLoader() throws NoSuchMethodException, IllegalAccessException {
        ClassLoader classLoader = MultiCore.getPlugin().getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            loadFail = true;
            throw new AssertionError("找不到URLClassLoader");
        }
        urlClassLoader = (URLClassLoader) classLoader;
        addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        addUrlMethod.setAccessible(true);
        addUrlMH = lookup.unreflect(addUrlMethod);
    }

    private void load(String name) throws Throwable {
        File toLoad = new File(libFolder, genJarName(name));
        addUrlMH.invoke(urlClassLoader, toLoad.toURI().toURL());
        MultiCore.info("成功加载库 " + toLoad.getName());
    }
}
