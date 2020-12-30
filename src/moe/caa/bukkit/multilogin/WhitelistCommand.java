package moe.caa.bukkit.multilogin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhitelistCommand implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp() || commandSender.hasPermission("multilogin.whitelist")){
            if(strings.length == 1){
                return Stream.of("add", "remove", "on", "off", "list").filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
            }
            if(strings.length == 2){
                if(strings[0].equalsIgnoreCase("remove")){
                    return PluginData.listWhitelist().stream().filter(s1 -> s1.startsWith(strings[1])).collect(Collectors.toList());
                }

            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp() || commandSender.hasPermission("multilogin.whitelist")){
            if(strings.length > 0){
                if(strings[0].equalsIgnoreCase("add")){
                    if(strings.length == 2){
                        executeAdd(commandSender, strings);
                        return true;
                    }
                } else if(strings[0].equalsIgnoreCase("remove")){
                    if(strings.length == 2){
                        executeRemove(commandSender, strings);
                        return true;
                    }
                } else if(strings[0].equalsIgnoreCase("on")){
                    if(strings.length == 1){
                        executeOn(commandSender, strings);
                        return true;
                    }
                } else if(strings[0].equalsIgnoreCase("off")){
                    if(strings.length == 1){
                        executeOff(commandSender, strings);
                        return true;
                    }
                } else if(strings[0].equalsIgnoreCase("list")){
                    if(strings.length == 1){
                        executeList(commandSender, strings);
                        return true;
                    }
                }
            }
            commandSender.sendMessage("无效的命令，请检查");
        } else {
            commandSender.sendMessage("无权限");
        }
        return true;
    }

    private void executeAdd(CommandSender sender, String[] args) {
        if(PluginData.addWhitelist(args[1])){
            sender.sendMessage("已经添加白名单");
        } else {
            sender.sendMessage("请不要重复添加");
        }
    }

    private void executeRemove(CommandSender sender, String[] args) {
        if(PluginData.removeWhitelist(args[1])){
            sender.sendMessage("已经移除白名单");
        } else {
            sender.sendMessage("请不要重复移除");
        }
    }

    private void executeOn(CommandSender sender, String[] args) {
        if(!PluginData.isWhitelist()){
            PluginData.setWhitelist(true);
            sender.sendMessage("已经开启白名单");
        } else {
            sender.sendMessage("请不要重复开启白名单");
        }
    }

    private void executeOff(CommandSender sender, String[] args) {
        if(PluginData.isWhitelist()){
            PluginData.setWhitelist(false);
            sender.sendMessage("已经关闭白名单");
        } else {
            sender.sendMessage("请不要重复关闭白名单");
        }
    }

    private void executeList(CommandSender sender, String[] args) {
        List<String> stringList = PluginData.listWhitelist();
        if(stringList.size() <= 0){
            sender.sendMessage("白名单内没有玩家");
        } else {
            sender.sendMessage(String.format("白名单一共有%d名玩家：%s", stringList.size(), String.join(", ", stringList.toArray(new String[0]))));
        }
    }

}
