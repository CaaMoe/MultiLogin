package moe.caa.bukkit.multilogin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhitelistCommand implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp() || commandSender.hasPermission("multilogin.whitelist.tab")){
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
                    executeOn(commandSender);
                    return true;
                }
            } else if(strings[0].equalsIgnoreCase("off")){
                if(strings.length == 1){
                    executeOff(commandSender);
                    return true;
                }
            } else if(strings[0].equalsIgnoreCase("list")){
                if(strings.length == 1){
                    executeList(commandSender);
                    return true;
                }
            }
        }
        commandSender.sendMessage(PluginData.getConfigurationConfig().getString("msgInvCmd"));
        return true;
    }

    private void executeAdd(CommandSender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.add")){
            if(PluginData.addWhitelist(args[1])){
                sender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelist"), args[1]));
            } else {
                sender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelistAlready"), args[1]));
            }
        }
    }

    private void executeRemove(CommandSender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.remove")){
            if(PluginData.removeWhitelist(args[1])){
                sender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelist"), args[1]));
            } else {
                sender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelistAlready"), args[1]));
            }
        }

    }

    private void executeOn(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.on")){
            if(!PluginData.isWhitelist()){
                PluginData.setWhitelist(true);
                sender.sendMessage(PluginData.getConfigurationConfig().getString("msgOpenWhitelist"));
            } else {
                sender.sendMessage(PluginData.getConfigurationConfig().getString("msgOpenWhitelistAlready"));
            }
        }
    }

    private void executeOff(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.off")){
            if(PluginData.isWhitelist()){
                PluginData.setWhitelist(false);
                sender.sendMessage(PluginData.getConfigurationConfig().getString("msgCloseWhitelist"));
            } else {
                sender.sendMessage(PluginData.getConfigurationConfig().getString("msgCloseWhitelistAlready"));
            }
        }

    }

    private void executeList(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.list")){
            List<String> stringList = PluginData.listWhitelist();
            if(stringList.size() <= 0){
                sender.sendMessage(PluginData.getConfigurationConfig().getString("msgWhitelistListNoth"));
            } else {
                List<String> list = PluginData.listWhitelist();
                sender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgWhitelistListN"), list.size(), String.join(", ", list)));
            }
        }
    }

    private boolean testPermission(CommandSender sender, String permission){
        if(sender.isOp() || sender.hasPermission(permission)){
            return true;
        }
        sender.sendMessage(PluginData.getConfigurationConfig().getString("msgNoPermission"));
        return false;
    }
}
