package moe.caa.multilogin.core.loader.main;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

public class PriorURLClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final Set<String> packageName;

    protected PriorURLClassLoader(URL[] urls, ClassLoader parent, Set<String> packageName) {
        super(urls, parent);
        this.packageName = packageName;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (containPrior(name)) {
                    try {
                        c = findClass(name);
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    private boolean containPrior(String name) {
        for (String s : packageName) {
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
