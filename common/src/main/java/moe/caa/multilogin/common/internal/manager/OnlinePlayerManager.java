package moe.caa.multilogin.common.internal.manager;

import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.main.MultiCore;

import java.util.Map;

public abstract class OnlinePlayerManager {
    public final MultiCore core;

    protected OnlinePlayerManager(MultiCore core) {
        this.core = core;
    }

    public abstract OnlinePlayer getPlayerExactByName(String name);

    public abstract Map<String, OnlinePlayer> getOnlinePlayers();
}
