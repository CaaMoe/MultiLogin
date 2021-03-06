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
import moe.caa.multilogin.core.data.databse.handler.UserDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.I18n;

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
            commandSender.sendMessage(PluginData.configurationConfig.getString("msgReload").get());
        }
    }

    /**
     * 处理命令“multilogin query [target]”
     */
    public static void executeQuery(ISender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = (strings.length == 2) ? strings[1] : (commandSender.isPlayer() ? commandSender.getSenderName() : null);
            if (s == null) {

                commandSender.sendMessage(PluginData.configurationConfig.getString("msgNoPlayer").get());
                return;
            }
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                try {
                    List<UserEntry> userList = UserDataHandler.getUserEntryByCurrentName(s);
                    try {
                        UUID uuid = UUID.fromString(s);
                        UserEntry byUuid = UserDataHandler.getUserEntryByOnlineUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                        byUuid = UserDataHandler.getUserEntryByRedirectUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                    } catch (IllegalArgumentException ignore) {
                    }

                    if (userList.size() > 0) {
                        for (UserEntry entry : userList) {
                            commandSender.sendMessage(String.format(PluginData.configurationConfig.getString("msgYDQuery").get(), s, entry.getServiceEntry().getName()));
                        }
                    } else {
                        commandSender.sendMessage(String.format(PluginData.configurationConfig.getString("msgYDQueryNoRel").get(), s));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                    commandSender.sendMessage(I18n.getTransString("plugin_severe_command"));
                }
            }, 0);
        }
    }

}
