package moe.caa.multilogin.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiLoginCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length > 0){
            if(strings[0].equalsIgnoreCase("query")){
                if(strings.length <= 2){
                    executeQuery(commandSender, strings);
                    return true;
                }
            } else if(strings[0].equalsIgnoreCase("reload")){
                if(strings.length == 1){
                    executeReload(commandSender);
                    return true;
                }
            }
        }
        commandSender.sendMessage(PluginData.getConfigurationConfig().getString("msgInvCmd"));
        return true;
    }

    private void executeQuery(CommandSender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = strings.length == 2 ? strings[1] : ((commandSender instanceof Player) ? commandSender.getName() : null);
            if(s != null){
                PluginData.UserEntry entry = PluginData.getUserEntry(s);
                if(entry != null){
                    commandSender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgYDQuery"), s, entry.getYggServerDisplayName()));
                } else {
                    commandSender.sendMessage(String.format(PluginData.getConfigurationConfig().getString("msgYDQueryNoRel"), s));
                }
            } else {
                commandSender.sendMessage(PluginData.getConfigurationConfig().getString("msgNoPlayer"));
            }
        }

    }

    private void executeReload(CommandSender commandSender)  {
        if (testPermission(commandSender, "multilogin.multilogin.reload")) {
            try {
                PluginData.reloadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            commandSender.sendMessage(PluginData.getConfigurationConfig().getString("msgReload"));
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp() || commandSender.hasPermission("multilogin.multilogin.tab")){
            if(strings.length == 1){
                return Stream.of("query", "reload").filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private boolean testPermission(CommandSender sender, String permission){
        if(sender.isOp() || sender.hasPermission(permission)){
            return true;
        }
        sender.sendMessage(PluginData.getConfigurationConfig().getString("msgNoPermission"));
        return false;
    }
}
