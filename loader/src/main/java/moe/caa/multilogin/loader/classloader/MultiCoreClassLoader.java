package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public class MultiCoreClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    public MultiCoreClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
