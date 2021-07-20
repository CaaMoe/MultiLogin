package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;

public class MultiLoginCommand {

    public static void register(CommandDispatcher<ISender> dispatcher){
        dispatcher.register(
                CommandHandler.literal("multilogin")
                        .then(CommandHandler.literal("query")
                                .then(CommandHandler.literal("name")

                                ).then(CommandHandler.literal("onlineuuid")

                                ).then(CommandHandler.literal("redirectuuid")

                                )
                        ).requires(Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY::hasPermission)
                        .then(CommandHandler.literal("reload").requires(Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD::hasPermission)

                        )

        );

    }
}
