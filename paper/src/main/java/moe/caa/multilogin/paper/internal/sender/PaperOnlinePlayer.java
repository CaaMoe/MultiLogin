package moe.caa.multilogin.paper.internal.sender;

import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PaperOnlinePlayer extends PaperSender<Player> implements OnlinePlayer {
    PaperOnlinePlayer(Player handle) {
        super(handle);
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public OnlineData getOnlineData() {
        return MultiLoginPaperMain.getInstance().getOnlinePlayerManager().onlineDataMap.get(
                ((CraftPlayer) handle).getHandle().connection.connection
        );
    }
}
