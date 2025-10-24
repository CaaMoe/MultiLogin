package moe.caa.multilogin.common.internal.manager;

import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.main.MultiCore;

public abstract class OnlinePlayerManager {
    public final MultiCore core;

    protected OnlinePlayerManager(MultiCore core) {
        this.core = core;
    }

    public abstract OnlinePlayer getPlayerExactByName(String name);
}
