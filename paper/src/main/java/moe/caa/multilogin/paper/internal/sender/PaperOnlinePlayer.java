package moe.caa.multilogin.paper.internal.sender;

import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.util.CookieKey;
import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
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

    @Override
    public void writeCookie(CookieKey key, byte[] cookie) {
        handle.storeCookie(new NamespacedKey(key.namespace(), key.key()), cookie);
    }

    @Override
    public void transfer(InetSocketAddress address) {
        handle.transfer(address.getHostString(), address.getPort());
    }

    public InetSocketAddress getConnectedServerAddress() {
        return handle.getConnection().getVirtualHost();
    }
}
