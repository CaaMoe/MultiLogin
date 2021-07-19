package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.command.commands.multilogin.MainMultiLoginCommand;
import moe.caa.multilogin.core.command.commands.whitelist.MainWhitelistCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandHandler {
    private static final Set<SubCommand> rootCommand = new HashSet<>();

    static {
        rootCommand.add(new MainWhitelistCommand());
        rootCommand.add(new MainMultiLoginCommand());
    }

    public static void execute(ISender sender, String command, String[] args) {
        for (SubCommand subCommand : rootCommand) {
            if (subCommand.name.equalsIgnoreCase(command)) {
                try {
                    subCommand.execute0(sender, args);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return;
            }
        }
        sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
    }

    public static List<String> tabCompile(ISender sender, String command, String[] args) {
        for (SubCommand subCommand : rootCommand) {
            if (subCommand.name.equalsIgnoreCase(command)) {
                try {
                    return subCommand.tabCompile0(sender, args);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }
}
