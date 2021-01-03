package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.RefUtil;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        System.out.println("aaaaaaaaaa");
        event.getConnection().setOnlineMode(true);

        try {
            RefUtil.modify(event);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
