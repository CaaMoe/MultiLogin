package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 表示一个插件的类加载器
 */
public interface IExtURLClassLoader {
    void addURL(URL url);

    URLClassLoader self();

    Class<?> defineClass(String name, byte[] bytes);
}
