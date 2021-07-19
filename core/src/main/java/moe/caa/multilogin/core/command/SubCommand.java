package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SubCommand {
    protected final String name;
    protected final Permission permission;
    protected final Set<SubCommand> subCommands = new HashSet<>();

    protected SubCommand(String name, Permission permission) {
        this.name = name;
        this.permission = permission;
    }

    public final void execute0(ISender sender, String[] args) throws Throwable {
        if (permission != null && !permission.hasPermissionAndFeedback(sender)) return;
        if (args.length >= 1) {
            String name = args[0];
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            for (SubCommand command : subCommands) {
                if (command.name.equalsIgnoreCase(name)) {
                    command.execute0(sender, newArgs);
                    return;
                }
            }
        }
        execute(sender, args);
    }

    public final List<String> tabCompile0(ISender sender, String[] args) throws Throwable {
        if (permission != null && !permission.hasPermission(sender)) return Collections.emptyList();
        if (args.length > 1) {
            String name = args[0];
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            for (SubCommand command : subCommands) {
                if (command.name.equalsIgnoreCase(name)) {
                    return command.tabCompile0(sender, newArgs);
                }
            }
        }
        return tabCompile(sender, args);
    }

    public void execute(ISender sender, String[] args) throws Throwable {
        sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
    }

    public List<String> tabCompile(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            return subCommands.stream().map(subCommand -> subCommand.name).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
