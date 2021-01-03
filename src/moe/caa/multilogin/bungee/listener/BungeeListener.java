package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.RefUtil;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;

public class BungeeListener implements Listener {

    private void onPreLogin(PreLoginEvent event){
        event.getConnection().setOnlineMode(true);
        try {
            RefUtil.modify(event);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
