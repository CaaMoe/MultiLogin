package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import fun.ksnb.multilogin.velocity.impl.VelocitySender;

import java.util.List;

/**
 * Velocity 的指令处理程序
 */
public class CommandHandler {
    private final MultiLoginVelocity multiLoginVelocity;

    private final SimpleCommand simpleCommand = new SimpleCommand() {
        @Override
        public void execute(Invocation invocation) {
            String[] arguments = invocation.arguments();
            String[] ns = new String[arguments.length + 1];
            System.arraycopy(arguments, 0, ns, 1, arguments.length);
            ns[0] = invocation.alias();
            multiLoginVelocity.getMultiCoreAPI().getCommandHandler().execute(new VelocitySender(invocation.source()), ns);
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            String[] arguments = invocation.arguments();
            String[] ns = new String[arguments.length + 1];
            System.arraycopy(arguments, 0, ns, 1, arguments.length);
            ns[0] = invocation.alias();
            return multiLoginVelocity.getMultiCoreAPI().getCommandHandler().tabComplete(new VelocitySender(invocation.source()), ns);
        }
    };

    public CommandHandler(MultiLoginVelocity multiLoginVelocity) {
        this.multiLoginVelocity = multiLoginVelocity;
    }

    public void register(String cmdName) {
        CommandManager commandManager = multiLoginVelocity.getServer().getCommandManager();
        commandManager.register(commandManager.metaBuilder(cmdName).build(), simpleCommand);
    }
}
