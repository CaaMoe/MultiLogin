package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.bukkit.impl.BukkitSender;
import org.bukkit.command.*;

import java.util.List;
import java.util.Objects;

public class CommandHandler {
    private final MultiLoginBukkit multiLoginBukkit;

    public CommandHandler(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
    }

    public void register() {
        PluginCommand pluginCommand = Objects.requireNonNull(multiLoginBukkit.getCommand(multiLoginBukkit.getName().toLowerCase()));
        Executor executor = new Executor();
        pluginCommand.setTabCompleter(executor);
        pluginCommand.setExecutor(executor);
    }

    private class Executor implements CommandExecutor, TabCompleter {

        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            String[] ns = new String[strings.length + 1];
            System.arraycopy(strings, 0, ns, 1, strings.length);
            ns[0] = command.getName();
            multiLoginBukkit.getMultiCoreAPI().getCommandHandler().execute(new BukkitSender(commandSender), ns);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
            String[] ns = new String[strings.length + 1];
            System.arraycopy(strings, 0, ns, 1, strings.length);
            ns[0] = command.getName();
            return multiLoginBukkit.getMultiCoreAPI().getCommandHandler().tabComplete(new BukkitSender(commandSender), ns);
        }
    }
}
