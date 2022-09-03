package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.util.Collections;

/**
 * 所有优先类加载器
 */
public class PriorAllURLClassLoader extends PriorURLClassLoader {
    static {
        registerAsParallelCapable();
    }

    public PriorAllURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent, Collections.emptySet());
    }

    @Override
    public boolean containPrior(String name) {
        return true;
    }

    @Override
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
