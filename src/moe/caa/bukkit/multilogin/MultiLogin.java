package moe.caa.bukkit.multilogin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.caa.bukkit.multilogin.listener.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MultiLogin extends JavaPlugin {
    protected static MultiLogin INSTANCE;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        MultiLogin.INSTANCE = this;
        try {
            NMSUtil.initService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null){
            PacketListener.register(this);
        }
        try {
            PluginData.reloadConfig();
            PluginData.readData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        try {
            PluginData.saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
