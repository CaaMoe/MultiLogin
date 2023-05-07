package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 所有优先类加载器
 */
public class PriorAllURLClassLoader extends PriorURLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final Set<String> ignored;

    public PriorAllURLClassLoader(URL[] urls, ClassLoader parent, Set<String> ignored) {
        super(urls, parent, Collections.emptySet());
        this.ignored = new HashSet<>(ignored);
    }

    public PriorAllURLClassLoader(URL[] urls, ClassLoader parent) {
        this(urls, parent, new HashSet<>());
    }

    @Override
    public boolean containPrior(String name) {
        return !containIgnore(name);
    }

    @Override
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }

    private boolean containIgnore(String name) {
        for (String s : ignored) {
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
