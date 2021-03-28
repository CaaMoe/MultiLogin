/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.expansions.MultiLoginPlaceholderExpansion
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiLoginPlaceholderExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return ((JavaPlugin) MultiCore.getPlugin()).getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return String.join(", ", ((JavaPlugin) MultiCore.getPlugin()).getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return MultiCore.getPlugin().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        String ret = "";
        if (player == null || PluginData.isEmpty(params)) return ret;
        try {
            UserEntry entry = MultiLoginBukkit.USER_CACHE.get(player.getUniqueId());
            if (params.equalsIgnoreCase("currentname")) {
                ret = entry.getCurrent_name();
            } else if (params.equalsIgnoreCase("onlineuuid")) {
                ret = entry.getOnline_uuid().toString();
            } else if (params.equalsIgnoreCase("redirecteduuid")) {
                ret = entry.getRedirect_uuid().toString();
            } else if (params.equalsIgnoreCase("whitelist")) {
                ret = String.valueOf(entry.hasWhitelist());
            } else if (params.equalsIgnoreCase("yggdrasilname")) {
                ret = entry.getServiceEntry().getName();
            } else if (params.equalsIgnoreCase("yggdrasilpath")) {
                ret = entry.getServiceEntry().getPath();
            }
        } catch (Exception ignored) {
        }
        return ret;
    }
}
