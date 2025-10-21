package moe.caa.multilogin.paper.internal.online;

import moe.caa.multilogin.common.internal.online.OnlineData;
import moe.caa.multilogin.common.internal.online.OnlinePlayer;
import moe.caa.multilogin.paper.internal.command.PaperSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PaperOnlinePlayer extends PaperSender<Player> implements OnlinePlayer {
    public PaperOnlinePlayer(Player handle) {
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
        return null;
    }
}
