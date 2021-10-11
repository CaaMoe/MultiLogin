package moe.caa.multilogin.bukkit.support.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import moe.caa.multilogin.bukkit.auth.BukkitAuthCore;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkitPluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.ValueUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MultiLoginExpansion extends PlaceholderExpansion {
    private final MultiLoginBukkitPluginBootstrap bootstrap;

    public MultiLoginExpansion(MultiLoginBukkitPluginBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "multilogin";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", bootstrap.getDescriptionFile().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return bootstrap.getPluginVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String ret = "";
        if (player == null || ValueUtil.isEmpty(params)) return ret;
        try {
            User entry = null;
            for (User user : BukkitAuthCore.getUsers()) {
                if (user.getRedirectUuid().equals(player.getUniqueId())) {
                    entry = user;
                    break;
                }
            }
            if (entry == null) return "";
            if (params.equalsIgnoreCase("currentname")) {
                ret = entry.getCurrentName();
            } else if (params.equalsIgnoreCase("onlineuuid")) {
                ret = entry.getOnlineUuid().toString();
            } else if (params.equalsIgnoreCase("redirecteduuid")) {
                ret = entry.getRedirectUuid().toString();
            } else if (params.equalsIgnoreCase("whitelist")) {
                ret = String.valueOf(entry.isWhitelist());
            } else if (params.equalsIgnoreCase("yggdrasilname")) {
                ret = bootstrap.getCore().getYggdrasilServicesHandler().getYggdrasilService(entry.getYggdrasilService()).getName();
            } else if (params.equalsIgnoreCase("yggdrasilpath")) {
                ret = entry.getYggdrasilService();
            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Variable error.", e);
            ret = "VAR ERR";
        }
        return ret;
    }
}
