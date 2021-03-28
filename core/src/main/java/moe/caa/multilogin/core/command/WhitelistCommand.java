/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.WhitelistCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.data.databse.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.databse.handler.UserDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.I18n;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.UUID;

import static moe.caa.multilogin.core.command.CommandMain.testPermission;

/**
 * 命令处理器
 */
public class WhitelistCommand {

    /**
     * 处理命令“whitelist add target”
     */
    public static void executeAdd(ISender sender, String[] args) {
        if (testPermission(sender, "multilogin.whitelist.add")) {
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                boolean flag = false;
                do {
                    try {
                        List<UserEntry> userEntries = UserDataHandler.getUserEntryByCurrentName(args[1]);
                        for (UserEntry entry : userEntries) {
                            if (!entry.hasWhitelist()) {
                                entry.setWhitelist(true);
                                UserDataHandler.updateUserEntry(entry);
                                flag = true;
                                break;
                            }
                        }
                        if (userEntries.size() != 0) {
                            break;
                        }
                        if (!flag) {
                            UUID uuid = UUID.fromString(args[1]);
                            UserEntry byUuid = UserDataHandler.getUserEntryByOnlineUuid(uuid);
                            if (byUuid != null && !byUuid.hasWhitelist()) {
                                byUuid.setWhitelist(true);
                                UserDataHandler.updateUserEntry(byUuid);
                                flag = true;
                                break;
                            }

                            byUuid = UserDataHandler.getUserEntryByRedirectUuid(uuid);
                            if (byUuid != null && !byUuid.hasWhitelist()) {
                                byUuid.setWhitelist(true);
                                UserDataHandler.updateUserEntry(byUuid);
                                flag = true;
                                break;
                            }

                        }
                    } catch (IllegalArgumentException ignored) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                        sender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                        return;
                    }
                    if (!flag) {
                        flag = CacheWhitelistDataHandler.addCacheWhitelist(args[1]);
                    }
                } while (false);

                if (flag) {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgAddWhitelist"), args[1])));
                } else {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgAddWhitelistAlready"), args[1])));
                }
            }, 0);
        }
    }

    /**
     * 处理命令“whitelist remove target”
     */
    public static void executeRemove(ISender sender, String[] args) {
        if (testPermission(sender, "multilogin.whitelist.remove")) {
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                boolean flag = CacheWhitelistDataHandler.removeCacheWhitelist(args[1]);
                try {
                    List<UserEntry> userEntries = UserDataHandler.getUserEntryByCurrentName(args[1]);
                    for (UserEntry entry : userEntries) {
                        if (entry.hasWhitelist()) {
                            entry.setWhitelist(false);
                            UserDataHandler.updateUserEntry(entry);
                            MultiCore.getPlugin().kickPlayer(entry.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                            flag = true;
                        }
                    }

                    UUID uuid = UUID.fromString(args[1]);

                    UserEntry byUuid = UserDataHandler.getUserEntryByOnlineUuid(uuid);
                    if (byUuid != null && byUuid.hasWhitelist()) {
                        byUuid.setWhitelist(false);
                        UserDataHandler.updateUserEntry(byUuid);
                        MultiCore.getPlugin().kickPlayer(byUuid.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                        flag = true;
                    }

                    byUuid = UserDataHandler.getUserEntryByRedirectUuid(uuid);
                    if (byUuid != null && byUuid.hasWhitelist()) {
                        byUuid.setWhitelist(false);
                        UserDataHandler.updateUserEntry(byUuid);
                        MultiCore.getPlugin().kickPlayer(byUuid.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                        flag = true;
                    }

                } catch (IllegalArgumentException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                    sender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                    return;
                }
                if (flag) {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgDelWhitelist"), args[1])));
                } else {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgDelWhitelistAlready"), args[1])));
                }
            }, 0);
        }

    }

    /**
     * 处理命令“whitelist on”
     */
    public static void executeOn(ISender sender) {
        if (testPermission(sender, "multilogin.whitelist.on"))
            if (!PluginData.isWhitelist()) {
                PluginData.setWhitelist(true);
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgOpenWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgOpenWhitelistAlready")));
            }
    }

    /**
     * 处理命令“whitelist off”
     */
    public static void executeOff(ISender sender) {
        if (testPermission(sender, "multilogin.whitelist.off"))
            if (PluginData.isWhitelist()) {
                PluginData.setWhitelist(false);
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgCloseWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgCloseWhitelistAlready")));
            }
    }
}
