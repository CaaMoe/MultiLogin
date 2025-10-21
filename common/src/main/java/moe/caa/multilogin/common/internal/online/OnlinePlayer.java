package moe.caa.multilogin.common.internal.online;

import moe.caa.multilogin.common.internal.command.Sender;

import java.util.UUID;

public interface OnlinePlayer extends Sender {
    UUID getUniqueId();

    String getName();

    OnlineData getOnlineData();
}
