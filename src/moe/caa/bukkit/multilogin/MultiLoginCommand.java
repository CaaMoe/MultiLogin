package moe.caa.bukkit.multilogin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class MultiLoginCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp() || commandSender.hasPermission("multilogin.multilogin")){
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
        } else {
            commandSender.sendMessage("无权限");
        }
        return true;
    }

    private void executeQuery(CommandSender commandSender, String[] strings) {
        String s = strings.length == 2 ? strings[1] : ((commandSender instanceof Player) ? commandSender.getName() : null);
        if(s != null){
            PluginData.UserEntry entry = PluginData.getUserEntry(s);
            if(entry != null){
                // TODO
            } else {

            }
        } else {
            commandSender.sendMessage("控制台不能执行当前命令");
        }
    }

    private void executeReload(CommandSender commandSender)  {
        try {
            PluginData.reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        commandSender.sendMessage("已经重新加载");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
