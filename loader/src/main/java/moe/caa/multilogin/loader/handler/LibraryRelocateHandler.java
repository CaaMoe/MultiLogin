package moe.caa.multilogin.loader.handler;

import moe.caa.multilogin.loader.classloader.MultiCoreClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Map;

public class LibraryRelocateHandler {
    private static final String RELOCATED_PREFIX = "moe.caa.multilogin.libraries";
    private static final Map<String, String> SHOULD_RELOCATIONS = Map.ofEntries(
            generateRelocation("kotlin")
    );
    private static Class<?> jarRelocatorClass;
    private static Constructor<?> jarRelocatorConstructor;
    private static Method jarRelocatorRunMethod;
    private final File input;
    private final File output;

    public LibraryRelocateHandler(File input, File output) {
        this.input = input;
        this.output = output;
    }

    private static Map.Entry<String, String> generateRelocation(String s) {
        return Map.entry(s, RELOCATED_PREFIX + "." + s);
    }

    public static void init(MultiCoreClassLoader loader) throws ClassNotFoundException, NoSuchMethodException {
        LibraryRelocateHandler.jarRelocatorClass = loader.loadClass("me.lucko.jarrelocator.JarRelocator");
        LibraryRelocateHandler.jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Map.class);
        LibraryRelocateHandler.jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");
    }

    public void relocate() throws Exception {
        if (!output.getParentFile().exists()) {
            Files.createDirectories(output.getParentFile().toPath());
        }

        Object instance = jarRelocatorConstructor.newInstance(input, output, SHOULD_RELOCATIONS);
        jarRelocatorRunMethod.invoke(instance);
    }
}
