package moe.caa.bukkit.multilogin.listener;

import moe.caa.bukkit.multilogin.MultiLogin;
import moe.caa.bukkit.multilogin.NMSUtil;
import moe.caa.bukkit.multilogin.PluginData;
import moe.caa.bukkit.multilogin.yggdrasil.MLGameProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.InvocationTargetException;

public class BukkitListener implements Listener {

    @SuppressWarnings("all")
    @EventHandler(ignoreCancelled = true)
    private void onLogin(PlayerLoginEvent event){
        try {
            String text = PluginData.getUserVerificationMessage((MLGameProfile)NMSUtil.getGameProfile(event.getPlayer()));
            if(text != null){
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(text);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(PluginData.getConfigurationConfig().getString("msgNoAdopt"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event){
        if(MultiLogin.INSTANCE.isUpdate() && (event.getPlayer().hasPermission("multilogin.update") || event.getPlayer().isOp())){
            event.getPlayer().sendMessage("§c插件 §eMultiLogin §c有新的版本发布，请及时下载更新！");
        }
    }
}
