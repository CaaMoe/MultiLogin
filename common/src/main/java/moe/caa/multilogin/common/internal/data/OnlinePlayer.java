package moe.caa.multilogin.common.internal.data;

import moe.caa.multilogin.common.internal.util.CookieKey;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface OnlinePlayer extends Sender {
    UUID getUniqueId();

    String getName();

    OnlineData getOnlineData();


    void writeCookie(CookieKey key, byte[] cookie);

    void transfer(InetSocketAddress address);

    InetSocketAddress getConnectedServerAddress();
}
