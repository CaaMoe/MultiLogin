package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;

public class GlobalListener {
    private final MultiLoginVelocity multiLoginVelocity;

    public GlobalListener(MultiLoginVelocity multiLoginVelocity) {
        this.multiLoginVelocity = multiLoginVelocity;
    }

    public void register() {
        multiLoginVelocity.getServer().getEventManager().register(multiLoginVelocity, this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerJoin(LoginEvent event) {
        multiLoginVelocity.getMultiCoreAPI().getCache().pushPlayerJoinGame(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getUsername()
        );
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        multiLoginVelocity.getMultiCoreAPI().getCache().pushPlayerQuitGame(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getUsername()
        );
    }
}
