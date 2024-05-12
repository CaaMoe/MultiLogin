package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;

public class MDataConvert {
    private final CommandHandler handler;

    public MDataConvert(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> builder) {
        return builder.requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_DATA_CONVERT))
                .then(handler.literal("fromFloodgateOwnLinkData")
                        .then(handler.literal("sqlite"))
                        .then(handler.literal("mysql"))
                );
    }
}
