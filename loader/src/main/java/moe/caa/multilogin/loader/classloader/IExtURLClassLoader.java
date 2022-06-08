package moe.caa.multilogin.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public interface IExtURLClassLoader {
    void addURL(URL url);

    URLClassLoader self();
}
