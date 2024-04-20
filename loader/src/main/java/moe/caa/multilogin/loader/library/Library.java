package moe.caa.multilogin.loader.library;

import java.io.File;
import java.util.List;

public record Library(
        String group,
        String name,
        String version
) {
    public static Library of(String str) {
        String[] strings = str.split(":");
        return new Library(strings[0], strings[1], strings[2]);
    }

    public File getFile(File folder) {
        return new File(folder, getUrl());
    }

    public static final List<Library> NECESSARY_LIBRARIES = List.of(
            new Library("org.jetbrains.kotlin", "kotlin-stdlib", "1.9.23")
    );

    public String getUrl() {
        return group.replace(".", "/")
                + "/" + name + "/" + version + "/"
                + getFileName();
    }

    public String getFileName() {
        return name + "-" + version + ".jar";
    }

    public String getDisplayName() {
        return group + ":" + name + ":" + version;
    }
}

