package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.bukkit.impl.BukkitSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {
    private final MultiLoginBukkitPluginBootstrap plugin;

    public CommandHandler(MultiLoginBukkitPluginBootstrap plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return plugin.getCore().getCommandHandler().tabComplete(new BukkitSender(commandSender), s, strings);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        plugin.getCore().getCommandHandler().execute(new BukkitSender(commandSender), s, strings);
        return true;
    }
}
