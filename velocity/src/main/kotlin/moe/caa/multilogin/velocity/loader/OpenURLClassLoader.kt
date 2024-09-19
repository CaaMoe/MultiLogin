package moe.caa.multilogin.velocity.loader

import java.net.URL
import java.net.URLClassLoader

class OpenURLClassLoader: URLClassLoader(arrayOf()) {
    public override fun addURL(url: URL) {
        super.addURL(url)
    }
}