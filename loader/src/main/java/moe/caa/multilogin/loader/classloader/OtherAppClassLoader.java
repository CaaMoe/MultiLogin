package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 另起的类加载器，与 AppClassLoader 同级并且互不干扰
 */
public class OtherAppClassLoader extends URLClassLoader implements IExtURLClassLoader {
    public static final ClassLoader extClassLoader = ClassLoader.getSystemClassLoader().getParent();

    static {
        registerAsParallelCapable();
    }

    public OtherAppClassLoader(URL[] urls) {
        super(urls, extClassLoader);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public URLClassLoader self() {
        return this;
    }

    @Override
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
