package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.NMSUtil;
import moe.caa.multilogin.bukkit.PluginData;
import moe.caa.multilogin.bukkit.MultiLogin;
import moe.caa.multilogin.bukkit.yggdrasil.MLGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class BukkitListener implements Listener {

    @SuppressWarnings("all")
    @EventHandler(ignoreCancelled = true)
    private void onLogin(PlayerLoginEvent event){
        try {
            String text = PluginData.getUserVerificationMessage((MLGameProfile) NMSUtil.getGameProfile(event.getPlayer()));
            if(text != null){
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(text);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(PluginData.getConfigurationConfig().getString("msgNoAdopt"));
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event){
        if(MultiLogin.INSTANCE.isUpdate() && (event.getPlayer().hasPermission("multilogin.update") || event.getPlayer().isOp())){
            event.getPlayer().sendMessage("§c插件 §eMultiLogin §c有新的版本发布，请及时下载更新！");
        }

        // 再一次
        try {
            if (PluginData.isNoRepeatedName() && ((MLGameProfile)NMSUtil.getGameProfile(event.getPlayer())).getYggService().getPath().equalsIgnoreCase(PluginData.getSafeIdService())) {
                String name = event.getPlayer().getName();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().equalsIgnoreCase(name)) {
                        if(!player.getUniqueId().equals(event.getPlayer().getUniqueId())){
                            player.kickPlayer(PluginData.getConfigurationConfig().getString("msgRushNameOnl"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getPlayer().kickPlayer(PluginData.getConfigurationConfig().getString("msgNoAdopt"));
        }
    }
}
