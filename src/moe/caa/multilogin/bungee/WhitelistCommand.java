package moe.caa.multilogin.bungee;

import moe.caa.multilogin.core.PluginData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class WhitelistCommand extends Command {
    public WhitelistCommand() {
        super("whitelist");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if(strings.length > 0){
            if(strings[0].equalsIgnoreCase("add")){
                if(strings.length == 2){
                    executeAdd(commandSender, strings);
                    return;
                }
            } else if(strings[0].equalsIgnoreCase("remove")){
                if(strings.length == 2){
                    executeRemove(commandSender, strings);
                    return;
                }
            } else if(strings[0].equalsIgnoreCase("on")){
                if(strings.length == 1){
                    executeOn(commandSender);
                    return;
                }
            } else if(strings[0].equalsIgnoreCase("off")){
                if(strings.length == 1){
                    executeOff(commandSender);
                    return;
                }
            } else if(strings[0].equalsIgnoreCase("list")){
                if(strings.length == 1){
                    executeList(commandSender);
                    return;
                }
            }
        }
        commandSender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgInvCmd")));
    }

    private void executeAdd(CommandSender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.add")){
            if(PluginData.addWhitelist(args[1])){
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelist"), args[1])));
            } else {
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelistAlready"), args[1])));
            }
        }
    }

    private void executeRemove(CommandSender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.remove")){
            if(PluginData.removeWhitelist(args[1])){
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelist"), args[1])));
            } else {
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelistAlready"), args[1])));
            }
        }

    }

    private void executeOn(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.on")){
            if(!PluginData.isWhitelist()){
                PluginData.setWhitelist(true);
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgOpenWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgOpenWhitelistAlready")));
            }
        }
    }

    private void executeOff(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.off")){
            if(PluginData.isWhitelist()){
                PluginData.setWhitelist(false);
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgCloseWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgCloseWhitelistAlready")));
            }
        }

    }

    private void executeList(CommandSender sender) {
        if(testPermission(sender, "multilogin.whitelist.list")){
            List<String> stringList = PluginData.listWhitelist();
            if(stringList.size() <= 0){
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgWhitelistListNoth")));
            } else {
                List<String> list = PluginData.listWhitelist();
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgWhitelistListN"), list.size(), String.join(", ", list))));
            }
        }
    }

    private boolean testPermission(CommandSender sender, String permission){
        if(sender.hasPermission(permission)){
            return true;
        }
        sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoPermission")));
        return false;
    }
}
