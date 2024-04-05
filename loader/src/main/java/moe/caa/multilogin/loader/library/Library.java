package moe.caa.multilogin.loader.library;

import java.io.File;

public record Library(String group, String name, String version) {
    public static Library of(String str, String split) {
        String[] strings = str.split(split);
        return new Library(strings[0], strings[1], strings[2]);
    }

    public File getFile(File folder) {
        return new File(folder, getUrl());
    }

    public String getUrl() {
        return group.replace(".", "/")
                + "/" + name + "/" + version + "/"
                + name + "-" + version + ".jar";
    }
}

