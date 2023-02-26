package fun.ksnb.multilogin.bungee.main;

import fun.ksnb.multilogin.bungee.impl.BungeeSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Bungee 的命令执行器
 */
public class CommandHandler {
    private final MultiLoginBungee multiLoginBungee;

    public CommandHandler(MultiLoginBungee multiLoginBungee) {
        this.multiLoginBungee = multiLoginBungee;
    }

    public void register(String cmdName) {
        Executor executor = new Executor(cmdName);
        multiLoginBungee.getProxy().getPluginManager().registerCommand(multiLoginBungee, executor);
    }

    private class Executor extends Command implements TabExecutor {
        public Executor(String name) {
            super(name);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            String[] ns = new String[args.length + 1];
            System.arraycopy(args, 0, ns, 1, args.length);
            ns[0] = getName();
            multiLoginBungee.getMultiCoreAPI().getCommandHandler().execute(new BungeeSender(sender), ns);
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            String[] ns = new String[args.length + 1];
            System.arraycopy(args, 0, ns, 1, args.length);
            ns[0] = getName();
            return multiLoginBungee.getMultiCoreAPI().getCommandHandler().tabComplete(new BungeeSender(sender), ns);
        }
    }
}
