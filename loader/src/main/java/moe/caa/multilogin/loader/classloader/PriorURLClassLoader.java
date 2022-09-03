package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * 优先类加载器
 */
public class PriorURLClassLoader extends URLClassLoader implements IExtURLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final Set<String> packageName;

    public PriorURLClassLoader(URL[] urls, ClassLoader parent, Set<String> packageName) {
        super(urls, parent);
        this.packageName = new HashSet<>(packageName);
    }

    @Override
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (containPrior(name)) {
                    try {
                        c = findClass(name);
                        if (resolve) resolveClass(c);
                        return c;
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public URLClassLoader self() {
        return this;
    }

    public boolean containPrior(String name) {
        for (String s : packageName) {
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
