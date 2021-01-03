package moe.caa.multilogin.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class MultiCore {
    private static IPlugin plugin;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static IConfiguration getConfig(){
        return plugin.getPluginConfig();
    }

    public static void saveDefaultConfig(){
        plugin.savePluginDefaultConfig();
    }

    public static void reloadConfig(){
        plugin.reloadPluginConfig();
    }


    public static IConfiguration yamlLoadConfiguration(InputStreamReader reader){
        return plugin.yamlLoadConfiguration(reader);
    }


    public static InputStream getResource(String path){
        return plugin.getPluginResource(path);
    }

    public static void kickPlayer(UUID uuid, String msg) {
        plugin.kickPlayer(uuid, msg);
    }

    public static IPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(IPlugin plugin) {
        MultiCore.plugin = plugin;
    }
}
