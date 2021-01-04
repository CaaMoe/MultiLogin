package moe.caa.multilogin.core;

import net.md_5.bungee.api.chat.TextComponent;

import java.io.IOException;
import java.util.List;

public class Command {

    public static void executeReload(ISender commandSender)  {
        if (testPermission(commandSender, "multilogin.multilogin.reload")) {
            try {
                PluginData.reloadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            commandSender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgReload")));
        }
    }

    public static void executeQuery(ISender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = strings.length == 2 ? strings[1] : ((commandSender.isPlayer()) ? commandSender.getSenderName() : null);
            if(s != null){
                PluginData.UserEntry entry = PluginData.getUserEntry(s);
                if(entry != null){
                    commandSender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgYDQuery"), s, entry.getYggServerDisplayName())));
                } else {
                    commandSender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgYDQueryNoRel"), s)));
                }
            } else {
                commandSender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoPlayer")));
            }
        }
    }

    public static void executeAdd(ISender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.add")){
            if(PluginData.addWhitelist(args[1])){
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelist"), args[1])));
            } else {
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgAddWhitelistAlready"), args[1])));
            }
        }
    }

    public static void executeRemove(ISender sender, String[] args) {
        if(testPermission(sender, "multilogin.whitelist.remove")){
            if(PluginData.removeWhitelist(args[1])){
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelist"), args[1])));
            } else {
                sender.sendMessage(new TextComponent(String.format(PluginData.getConfigurationConfig().getString("msgDelWhitelistAlready"), args[1])));
            }
        }

    }

    public static void executeOn(ISender sender) {
        if(testPermission(sender, "multilogin.whitelist.on")){
            if(!PluginData.isWhitelist()){
                PluginData.setWhitelist(true);
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgOpenWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgOpenWhitelistAlready")));
            }
        }
    }

    public static void executeOff(ISender sender) {
        if(testPermission(sender, "multilogin.whitelist.off")){
            if(PluginData.isWhitelist()){
                PluginData.setWhitelist(false);
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgCloseWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgCloseWhitelistAlready")));
            }
        }

    }

    public static void executeList(ISender sender) {
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

    public static boolean testPermission(ISender sender, String permission){
        if(sender.hasPermission(permission)){
            return true;
        }
        sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoPermission")));
        return false;
    }
}
