package moe.caa.multilogin.paper.internal.online;

import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.online.OnlinePlayer;
import moe.caa.multilogin.common.internal.online.OnlinePlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PaperOnlinePlayerManager extends OnlinePlayerManager {
    public PaperOnlinePlayerManager(MultiCore core) {
        super(core);
    }

    @Override
    public OnlinePlayer getPlayerExactByName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) return null;
        return new PaperOnlinePlayer(player);
    }
}
