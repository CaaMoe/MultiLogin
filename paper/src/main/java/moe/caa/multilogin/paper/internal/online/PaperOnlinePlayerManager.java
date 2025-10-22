package moe.caa.multilogin.paper.internal.online;

import com.google.common.collect.MapMaker;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.online.OnlineData;
import moe.caa.multilogin.common.internal.online.OnlinePlayer;
import moe.caa.multilogin.common.internal.online.OnlinePlayerManager;
import net.minecraft.network.Connection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentMap;

public class PaperOnlinePlayerManager extends OnlinePlayerManager {
    protected final ConcurrentMap<Connection, OnlineData> onlineDataMap = new MapMaker()
            .weakKeys()
            .makeMap();

    public PaperOnlinePlayerManager(MultiCore core) {
        super(core);
    }

    @Override
    public OnlinePlayer getPlayerExactByName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) return null;
        return new PaperOnlinePlayer(player);
    }

    public void putOnlineData(Connection connection, OnlineData onlineData) {
        onlineDataMap.put(connection, onlineData);
    }
}
