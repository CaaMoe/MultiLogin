package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.bukkit.impl.BukkitSender;
import moe.caa.multilogin.core.command.CommandArguments;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {
    private final MultiLoginBukkit plugin;

    public CommandHandler(MultiLoginBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return plugin.getCore().getCommandHandler().tabComplete(new BukkitSender(commandSender), new CommandArguments(command.getName(), strings));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        plugin.getCore().getCommandHandler().executeAsync(new BukkitSender(commandSender), new CommandArguments(command.getName(), strings));
        return true;
    }
}
