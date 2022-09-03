package moe.caa.multilogin.loader.classloader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;

/**
 * 可以中途重定向包名的类加载器
 */
public class RelocateClassLoader extends URLClassLoader implements IExtURLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final Set<String> relocates;
    private final String appendPrefix;

    public RelocateClassLoader(URL[] urls, Set<String> relocates, String appendPrefix, ClassLoader parent) {
        super(urls, parent);
        this.appendPrefix = appendPrefix;
        this.relocates = Collections.unmodifiableSet(relocates);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(appendPrefix)) {
            try {
                final String vanillaName = name.substring(appendPrefix.length());
                String path = vanillaName.replace('.', '/').concat(".class");
                final InputStream inputStream = getResourceAsStream(path);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int code;
                while ((code = inputStream.read()) != -1) {
                    baos.write(code);
                }
                byte[] bytes = baos.toByteArray();

                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(0);

                cr.accept(new ClassRemapper(cw, new AppendPrefixMapper()), ClassReader.EXPAND_FRAMES);

                bytes = cw.toByteArray();

                return defineClass(name, bytes, 0, bytes.length);
            } catch (Exception ignored) {
            }
        }
        return super.findClass(name);
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

    private class AppendPrefixMapper extends Remapper {

        @Override
        public String map(String internalName) {
            for (String relocate : relocates) {
                if (!internalName.startsWith(relocate.replace('.', '/'))) continue;
                return appendPrefix.replace('.', '/') + internalName;
            }
            return super.map(internalName);
        }
    }
}
