package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import fun.ksnb.multilogin.velocity.impl.VelocitySender;
import moe.caa.multilogin.core.command.CommandArguments;

import java.util.List;

public class MultiLoginCommand implements SimpleCommand {
    private final MultiLoginVelocityPluginBootstrap plugin;

    public MultiLoginCommand(MultiLoginVelocityPluginBootstrap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        plugin.getCore().getCommandHandler().
                executeAsync(new VelocitySender(invocation.source())
                        , new CommandArguments(invocation.alias(), invocation.arguments()));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        return plugin.getCore().getCommandHandler().tabComplete(new VelocitySender(source)
                , new CommandArguments(invocation.alias(), args));
    }
}
