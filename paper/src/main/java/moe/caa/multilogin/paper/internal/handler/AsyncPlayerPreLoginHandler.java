package moe.caa.multilogin.paper.internal.handler;

import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerPreLoginHandler implements Listener {
    private final MultiLoginPaperMain paperMain;

    public AsyncPlayerPreLoginHandler(MultiLoginPaperMain paperMain) {
        this.paperMain = paperMain;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        try {
            NamespacedKey cookieKey = new NamespacedKey("multilogin", "cookie");
            byte[] bytes = event.getConnection().retrieveCookie(cookieKey).get();


        } catch (Throwable t) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, paperMain.getCore().messageConfig.loginUnknownError.get());
        }
    }
}
