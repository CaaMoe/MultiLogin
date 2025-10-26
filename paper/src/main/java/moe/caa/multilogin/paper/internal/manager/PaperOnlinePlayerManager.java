package moe.caa.multilogin.paper.internal.manager;

import com.google.common.collect.MapMaker;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.manager.OnlinePlayerManager;
import moe.caa.multilogin.paper.internal.sender.PaperOnlinePlayer;
import moe.caa.multilogin.paper.internal.sender.PaperSender;
import net.minecraft.network.Connection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PaperOnlinePlayerManager extends OnlinePlayerManager {
    public final ConcurrentMap<Connection, OnlineData> onlineDataMap = new MapMaker()
            .weakKeys()
            .makeMap();

    public PaperOnlinePlayerManager(MultiCore core) {
        super(core);
    }

    @Override
    public OnlinePlayer getPlayerExactByName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) return null;
        return PaperSender.wrapSender(player);
    }

    @Override
    public Map<String, OnlinePlayer> getOnlinePlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Map<String, OnlinePlayer> map = new HashMap<>(players.size());
        for (Player player : players) {
            map.put(player.getName(), PaperOnlinePlayer.wrapSender(player));
        }
        return map;
    }

    public void putOnlineData(Connection connection, OnlineData onlineData) {
        onlineDataMap.put(connection, onlineData);
    }
}
