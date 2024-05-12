package moe.caa.multilogin.loader.main;

import lombok.Getter;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.main.MultiCoreAPI;
import moe.caa.multilogin.api.internal.plugin.IPlugin;
import moe.caa.multilogin.api.internal.util.IOUtil;
import moe.caa.multilogin.flows.workflows.ParallelFlows;
import moe.caa.multilogin.flows.workflows.Signal;
import moe.caa.multilogin.loader.classloader.IExtURLClassLoader;
import moe.caa.multilogin.loader.classloader.PriorAllURLClassLoader;
import moe.caa.multilogin.loader.exception.InitialFailedException;
import moe.caa.multilogin.loader.library.Library;
import moe.caa.multilogin.loader.task.LibraryDownloadFlows;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表示插件加载器
 */
public class PluginLoader {
    public static final String nestJarName = "MultiLogin-Core.JarFile";
    public static final String coreClassName = "moe.caa.multilogin.core.main.MultiCore";

    public static final Map<Library, String> libraryDigestMap;
    public static final Set<Library> libraries;
    public static final List<String> repositories;

    // 这里读取依赖数据
    static {
        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream(".digests");
             InputStreamReader isr = new InputStreamReader(resourceAsStream);
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            Map<Library, String> tMap = new HashMap<>();
            lnr.lines().filter(s -> !s.trim().isEmpty() && s.charAt(0) != '#').map(s -> s.split("="))
                    .forEach(ss -> tMap.put(Library.of(ss[0], ":"), ss[1]));
            libraryDigestMap = Collections.unmodifiableMap(tMap);
        } catch (Exception e) {
            throw new InitialFailedException(e);
        }

        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream("libraries");
             InputStreamReader isr = new InputStreamReader(resourceAsStream);
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            libraries = lnr.lines().filter(s -> !s.trim().isEmpty() && s.charAt(0) != '#')
                    .map(ss -> Library.of(ss, "\\s+")).collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            throw new InitialFailedException(e);
        }

        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream("repositories");
             InputStreamReader isr = new InputStreamReader(resourceAsStream);
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            LinkedList<String> tList = new LinkedList<>();
            lnr.lines().filter(s -> !s.trim().isEmpty() && s.charAt(0) != '#')
                    .map(s -> s.endsWith("/") ? s : s + '/')
                    .forEach(tList::add);

            repositories = Collections.unmodifiableList(tList);
        } catch (Exception e) {
            throw new InitialFailedException(e);
        }

        // 判断文件完整
        for (Library library : libraries) {
            if (!libraryDigestMap.containsKey(library)) {
                throw new InitialFailedException("Missing digest for file " + library.getFileName() + ".");
            }
        }
    }

    private final File librariesFolder;
    private final IPlugin plugin;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    @Getter
    private IExtURLClassLoader pluginClassLoader = new PriorAllURLClassLoader(new URL[0], PluginLoader.class.getClassLoader(),
            Stream.of("moe.caa.multilogin.", "java.", "net.minecraft.", "com.mojang.", "org.bukkit.").collect(Collectors.toSet()));
    @Getter
    private MultiCoreAPI coreObject;

    public PluginLoader(IPlugin plugin) {
        this.plugin = plugin;
        this.librariesFolder = new File(plugin.getDataFolder(), "libraries");
    }

    /**
     * 开始加载
     */
    public synchronized void load(String... additions) throws Exception {
        if (loaded.getAndSet(true)) {
            throw new UnsupportedOperationException("Repeated call.");
        }
        IOUtil.removeAllFiles(plugin.getTempFolder());
        generateFolder();

        List<Library> needDownload = new ArrayList<>();

        for (Library library : libraries) {
            File file = new File(librariesFolder, library.getFileName());
            if (file.exists() && file.length() != 0) {
                final String sha256 = getSha256(file);
                LoggerProvider.getLogger().debug(
                        String.format("The digest value of calculation file %s is %s.", file.getName(), sha256)
                );
                if (sha256.equals(libraryDigestMap.get(library))) {
                    pluginClassLoader.addURL(file.toURI().toURL());
                    continue;
                }
                LoggerProvider.getLogger().warn(
                        String.format("Failed to validate digest value of file %s, it will be re-downloaded.", file.getAbsolutePath())
                );
            }
            needDownload.add(library);
        }

        // 下载缺失文件
        if (needDownload.size() != 0) {
            LoggerProvider.getLogger().info(
                    String.format("Downloading %d missing files...", needDownload.size())
            );
            ParallelFlows<Void> downloadFlows = new ParallelFlows<>(needDownload.stream().map(library ->
                    new LibraryDownloadFlows(library, librariesFolder, plugin.getTempFolder())).collect(Collectors.toList())
            );
            final Signal run = downloadFlows.run(null);
            if (run == Signal.TERMINATED) {
                throw new InitialFailedException("Failed to download the missing file.");
            }
        }

        for (Library library : needDownload) {
            File file = new File(librariesFolder, library.getFileName());

            final String sha256 = getSha256(file);
            LoggerProvider.getLogger().debug(
                    String.format("The digest value of calculation file %s is %s.", file.getName(), sha256)
            );
            if (sha256.equals(libraryDigestMap.get(library))) {
                pluginClassLoader.addURL(file.toURI().toURL());
                continue;
            }
            throw new InitialFailedException(
                    String.format("Failed to validate the digest value of the file %s that was just downloaded.", file.getAbsolutePath())
            );
        }


        // 提取 nest jar
        loadNestJar(nestJarName, pluginClassLoader);


        for (String addition : additions) {
            loadNestJar(addition, pluginClassLoader);
        }

        loadCore();
    }

    private void loadNestJar(String nestJarName, IExtURLClassLoader classLoader) throws IOException {
        final File output = File.createTempFile(nestJarName + ".", ".jar", plugin.getTempFolder());
        if (!output.exists()) {
            Files.createFile(output.toPath());
        }
        output.deleteOnExit();
        try (InputStream is = PluginLoader.class.getClassLoader().getResourceAsStream(nestJarName);
             FileOutputStream fos = new FileOutputStream(output);
        ) {
            IOUtil.copy(Objects.requireNonNull(is, nestJarName), fos);
        }
        classLoader.addURL(output.toURI().toURL());
    }

    private void loadCore() throws Exception {
        Class<?> coreClass = findClass(coreClassName);
        for (Constructor<?> constructor : coreClass.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == IPlugin.class) {
                coreObject = (MultiCoreAPI) constructor.newInstance(plugin);
                return;
            }
        }
        throw new RuntimeException("Not found constructor");
    }


    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, pluginClassLoader.self());
    }

    /**
     * 关闭
     */
    public synchronized void close() throws Exception {
        if (pluginClassLoader != null) pluginClassLoader.self().close();
        plugin.getRunServer().getScheduler().shutdown();
        coreObject = null;
        pluginClassLoader = null;
        IOUtil.removeAllFiles(plugin.getTempFolder());
    }

    /**
     * 生成依赖和临时目录文件夹
     */
    private void generateFolder() throws IOException {
        if (!librariesFolder.exists() && !librariesFolder.mkdirs()) {
            throw new IOException(String.format("Unable to create folder: %s", librariesFolder.getAbsolutePath()));
        }
        if (!plugin.getTempFolder().exists() && !plugin.getTempFolder().mkdirs()) {
            throw new IOException(String.format("Unable to create folder: %s", plugin.getTempFolder().getAbsolutePath()));
        }
    }

    // 获得文件sha256
    private String getSha256(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            byte[] buff = new byte[1024];
            int n;
            while ((n = fis.read(buff)) > 0) {
                baos.write(buff, 0, n);
            }
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : digest) {
                String temp = Integer.toHexString((aByte & 0xFF));
                if (temp.length() == 1) {
                    sb.append("0");
                }
                sb.append(temp);
            }
            return sb.toString();
        }
    }
}
