package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.SimpleCommand;
import fun.ksnb.multilogin.velocity.impl.VelocitySender;

import java.util.List;

public class MultiLoginCommand implements SimpleCommand {
    private final MultiLoginVelocityPluginBootstrap plugin;

    public MultiLoginCommand(MultiLoginVelocityPluginBootstrap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        plugin.getCore().getCommandHandler().execute(new VelocitySender(invocation.source()), invocation.alias(), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return plugin.getCore().getCommandHandler().tabComplete(new VelocitySender(invocation.source()), invocation.alias(), invocation.arguments());
    }
}
