package moe.caa.multilogin.loader.library;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.main.PluginLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;

public class LibraryList {
    public static final String RELOCATED_PREFIX = "moe.caa.multilogin.libraries";
    public static Map<String, List<Library>> LIBRARY_GROUP;
    public static Map<String, String> RELOCATIONS;

    static {
        Map<String, List<Library>> libraryGroup = new HashMap<>();
        Map<String, String> relocations = new HashMap<>();

        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream("library_list");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(resourceAsStream));
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            List<Library> currentGroup = null;
            for (String string : lnr.lines().toList()) {
                if (string.startsWith("[group] ")) {
                    currentGroup = libraryGroup.merge(string.substring("[group] ".length()), new ArrayList<>(), (libraries, libraries2) -> {
                        ArrayList<Library> list = new ArrayList<>();
                        list.addAll(libraries);
                        list.addAll(libraries2);
                        return list;
                    });
                } else if (string.startsWith("[library] ") && currentGroup != null) {
                    currentGroup.add(Library.of(string.substring("[library] ".length())));
                } else if (string.startsWith("[relocate] ")) {
                    String s = string.substring("[relocate] ".length());
                    relocations.put(s, RELOCATED_PREFIX + "." + s);
                }
            }
        } catch (Throwable throwable) {
            LoggerProvider.logger.error("Unable to load internal file library_list", throwable);
        }

        LibraryList.LIBRARY_GROUP = Collections.unmodifiableMap(libraryGroup);
        LibraryList.RELOCATIONS = Collections.unmodifiableMap(relocations);
    }
}
