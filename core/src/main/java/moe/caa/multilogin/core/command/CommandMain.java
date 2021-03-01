package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.impl.ISender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static moe.caa.multilogin.core.data.data.PluginData.configurationConfig;

public class CommandMain {

    /**
     * 执行一个插件命令
     *
     * @param cmd     根命令
     * @param sender  命令执行者
     * @param strings 命令参数
     * @return 命令执行结果
     */
    public static boolean submitCommand(String cmd, ISender sender, String[] strings) {
        try {
            if (cmd.equalsIgnoreCase("whitelist")) {
                if (strings.length > 0)
                    if (strings[0].equalsIgnoreCase("add")) {
                        if (strings.length == 2) {
                            CommandHandler.executeAdd(sender, strings);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("remove")) {
                        if (strings.length == 2) {
                            CommandHandler.executeRemove(sender, strings);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("on")) {
                        if (strings.length == 1) {
                            CommandHandler.executeOn(sender);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("off")) {
                        if (strings.length == 1) {
                            CommandHandler.executeOff(sender);
                            return true;
                        }
                    }
            } else if (cmd.equalsIgnoreCase("multilogin") &&
                    strings.length > 0) {
                if (strings[0].equalsIgnoreCase("query")) {
                    if (strings.length <= 2) {
                        CommandHandler.executeQuery(sender, strings);
                        return true;
                    }
                } else if (strings[0].equalsIgnoreCase("reload") &&
                        strings.length == 1) {
                    CommandHandler.executeReload(sender);
                    return true;
                }
            }
            sender.sendMessage(new TextComponent(configurationConfig.getString("msgInvCmd")));
        } catch (Exception e) {
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe("执行命令时出现异常");
            sender.sendMessage(new TextComponent(ChatColor.RED + "执行命令时出现异常"));
        }
        return true;
    }

    /**
     * 请求一个命令建议
     *
     * @param cmd     根命令
     * @param sender  命令发送者
     * @param strings 参数
     * @return 建议列表
     */
    public static List<String> suggestCommand(String cmd, ISender sender, String[] strings) {
        if (cmd.equalsIgnoreCase("whitelist")) {
            if (sender.isOp() || sender.hasPermission("multilogin.whitelist.tab")) {
                if (strings.length == 1) {
                    return Stream.of(new String[]{"add", "remove", "on", "off"}).filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
                }
            }
        } else if (cmd.equalsIgnoreCase("multilogin") && (
                sender.isOp() || sender.hasPermission("multilogin.multilogin.tab")) &&
                strings.length == 1) {
            return Stream.of(new String[]{"query", "reload"}).filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
