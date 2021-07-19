package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;

public class WhitelistCommand {

    public static void register(CommandDispatcher<ISender> dispatcher){
        dispatcher.register(
                CommandHandler.literal("whitelist")
                .then(CommandHandler.literal("add").requires(Permission.MULTI_LOGIN_WHITELIST_ADD::hasPermission)
                    .then(CommandHandler.argument("target", StringArgumentType.string())
                        .executes(WhitelistCommand::executeAdd)
                    )
                )
                .then(CommandHandler.literal("remove").requires(Permission.MULTI_LOGIN_WHITELIST_REMOVE::hasPermission)
                        .then(CommandHandler.argument("target", StringArgumentType.string())
                                .executes(WhitelistCommand::executeRemove)
                        )
                )
        );
    }

    private static int executeAdd(CommandContext<ISender> command){
        return 0;
    }

    private static int executeRemove(CommandContext<ISender> command){
        return 0;
    }
}
