package moe.caa.multilogin.bukkit.support.expansions;

import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
public class MultiLoginPlaceholderExpansion extends PlaceholderExpansion {
    private final MultiLoginBukkit plugin;

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || ValueUtil.isEmpty(params)) return "";
        Pair<UUID, Integer> profile = plugin.getMultiCoreAPI()
                .getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
        if (profile == null) return "";
        if (params.equalsIgnoreCase("yggdrasilId")) {
            return String.valueOf(profile.getValue2());
        } else if (params.equalsIgnoreCase("yggdrasilName")) {
            String name = plugin.getMultiCoreAPI().getPlayerHandler().getYggdrasilName(profile.getValue2());
            return ValueUtil.isEmpty(name) ? "" : name;
        } else if (params.equalsIgnoreCase("onlineUUID")) {
            return profile.getValue1().toString();
        } else if (params.equalsIgnoreCase("ingameUUID")) {
            return player.getUniqueId().toString();
        }
        return "";
    }
}
