package fun.ksnb.multilogin.bungee.main;

import fun.ksnb.multilogin.bungee.impl.BungeeSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MultiLoginCommand extends Command implements TabExecutor {

    public MultiLoginCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MultiLoginBungeePluginBootstrap.getInstance().getCore().getCmdHandler().execute(new BungeeSender(sender), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return MultiLoginBungeePluginBootstrap.getInstance().getCore().getCmdHandler().tabComplete(new BungeeSender(sender), args);
    }
}
