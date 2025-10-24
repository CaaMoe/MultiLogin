package moe.caa.multilogin.common.internal.data;

import java.util.UUID;

public interface OnlinePlayer extends Sender {
    UUID getUniqueId();

    String getName();

    OnlineData getOnlineData();
}
