/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.MultiLoginCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.I18n;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.UUID;

import static moe.caa.multilogin.core.command.CommandMain.testPermission;

public class MultiLoginCommand {


    /**
     * 处理命令“multilogin reload”
     */
    public static void executeReload(ISender commandSender) {
        if (testPermission(commandSender, "multilogin.multilogin.reload")) {
            try {
                PluginData.reloadConfig();
            } catch (Exception e) {
                e.printStackTrace();
            }
            commandSender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgReload")));
        }
    }

    /**
     * 处理命令“multilogin query [target]”
     */
    public static void executeQuery(ISender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = (strings.length == 2) ? strings[1] : (commandSender.isPlayer() ? commandSender.getSenderName() : null);
            if (s == null) {

                commandSender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgNoPlayer")));
                return;
            }
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                try {
                    List<UserEntry> userList = SQLHandler.getUserEntryByCurrentName(s);
                    try {
                        UUID uuid = UUID.fromString(s);
                        UserEntry byUuid = SQLHandler.getUserEntryByOnlineUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                        byUuid = SQLHandler.getUserEntryByRedirectUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                    } catch (IllegalArgumentException ignore) {
                    }

                    if (userList.size() > 0) {
                        for (UserEntry entry : userList) {
                            commandSender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgYDQuery"), s, entry.getServiceEntry().getName())));
                        }
                    } else {
                        commandSender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgYDQueryNoRel"), s)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                    commandSender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                }
            }, 0);
        }
    }

}
