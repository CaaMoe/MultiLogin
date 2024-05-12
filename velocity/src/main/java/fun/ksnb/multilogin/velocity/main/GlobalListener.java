package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import fun.ksnb.multilogin.velocity.impl.VelocityPlayer;
import moe.caa.multilogin.api.internal.handle.HandleResult;
import net.kyori.adventure.text.Component;

/**
 * Velocity 的事件处理程序
 */
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
        HandleResult result = multiLoginVelocity.getMultiCoreAPI().getPlayerHandler().pushPlayerJoinGame(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getUsername()
        );
        if (result.getType() == HandleResult.Type.KICK) {
            if (result.getKickMessage() == null || result.getKickMessage().trim().length() == 0) {
                event.getPlayer().disconnect(Component.text(""));
            } else {
                event.getPlayer().disconnect(Component.text(result.getKickMessage()));
            }
            return;
        }

        multiLoginVelocity.getMultiCoreAPI().getPlayerHandler().callPlayerJoinGame(new VelocityPlayer(event.getPlayer()));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        multiLoginVelocity.getMultiCoreAPI().getPlayerHandler().pushPlayerQuitGame(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getUsername()
        );
    }
}
