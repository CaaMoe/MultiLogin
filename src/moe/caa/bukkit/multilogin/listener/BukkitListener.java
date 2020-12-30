package moe.caa.bukkit.multilogin.listener;

import moe.caa.bukkit.multilogin.NMSUtil;
import moe.caa.bukkit.multilogin.PluginData;
import moe.caa.bukkit.multilogin.yggdrasil.MLGameProfile;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.InvocationTargetException;

public class BukkitListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    private void onLogin(PlayerLoginEvent event){
        try {
            TextComponent text = PluginData.getUserVerificationMessage((MLGameProfile)NMSUtil.getGameProfile(event.getPlayer()));
            assert text != null;
            System.out.println(text.toString());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
