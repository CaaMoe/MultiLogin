package moe.caa.multilogin.loader.main;

import moe.caa.multilogin.api.core.IMultiCore;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.loader.library.Library;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;

public class PluginLoader {
    public static final Map<Library, String> DIGESTED_MAP;

    static {
        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream(".digested");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(resourceAsStream));
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            Map<Library, String> tMap = new HashMap<>();
            lnr.lines().map(s -> s.split("="))
                    .forEach(ss -> tMap.put(Library.of(ss[0], ":"), ss[1]));

            DIGESTED_MAP = Collections.unmodifiableMap(tMap);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to initialize internal data.");
        }
    }

    public final IPlugin plugin;
    private MultiCore multiCore;

    public PluginLoader(IPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        multiCore = new MultiCore(plugin);
        multiCore.enable();
    }

    public void disable() {
        Optional.ofNullable(multiCore).ifPresent(e -> e.disable());
    }

    public IMultiCore getMultiCore() {
        return multiCore;
    }
}
