package moe.caa.multilogin.bungee;

import moe.caa.multilogin.core.PluginData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;

public class MultiLoginCommand extends Command {


    public MultiLoginCommand() {
        super("multilogin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if(strings.length > 0){
            if(strings[0].equalsIgnoreCase("query")){
                if(strings.length <= 2){
                    executeQuery(commandSender, strings);
                    return;
                }
            } else if(strings[0].equalsIgnoreCase("reload")){
                if(strings.length == 1){
                    executeReload(commandSender);
                    return;
                }
            }
        }
        commandSender.sendMessage(PluginData.getConfigurationConfig().getString("msgInvCmd"));
    }


    private void executeReload(CommandSender commandSender)  {
        if (testPermission(commandSender, "multilogin.multilogin.reload")) {
            try {
                PluginData.reloadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            commandSender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgReload")));
        }

    }

    private void executeQuery(CommandSender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = strings.length == 2 ? strings[1] : ((commandSender instanceof ProxiedPlayer) ? commandSender.getName() : null);
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


    private boolean testPermission(CommandSender sender, String permission){
        if(sender.hasPermission(permission)){
            return true;
        }
        sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoPermission")));
        return false;
    }
}
