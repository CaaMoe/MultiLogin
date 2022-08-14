package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.bukkit.impl.BukkitSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Bukkit 的命令处理程序
 */
public class CommandHandler {
    private final MultiLoginBukkit multiLoginBukkit;

    private final TabExecutor executor = new TabExecutor() {
        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            String[] ns = new String[args.length + 1];
            System.arraycopy(args, 0, ns, 1, args.length);
            ns[0] = command.getName();
            return multiLoginBukkit.getMultiCoreAPI().getCommandHandler().tabComplete(new BukkitSender(sender), ns);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            String[] ns = new String[args.length + 1];
            System.arraycopy(args, 0, ns, 1, args.length);
            ns[0] = command.getName();
            multiLoginBukkit.getMultiCoreAPI().getCommandHandler().execute(new BukkitSender(sender), ns);
            return true;
        }
    };

    public CommandHandler(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
    }

    public void register(String cmdName) {
        Objects.requireNonNull(multiLoginBukkit.getCommand(cmdName)).setExecutor(executor);
        Objects.requireNonNull(multiLoginBukkit.getCommand(cmdName)).setTabCompleter(executor);
    }
}
