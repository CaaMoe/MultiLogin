package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlineArgumentType;
import moe.caa.multilogin.core.command.argument.ProfileArgumentType;

public class MFindCommand {
    private final CommandHandler handler;

    public MFindCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(
                handler.literal("profile")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_FIND_PROFILE))
                        .then(handler.argument("profile", ProfileArgumentType.profile()))
                        .executes(this::executeProfile))
                .then(handler.literal("online")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_FIND_ONLINE))
                        .then(handler.argument("online", OnlineArgumentType.online()))
                        .executes(this::executeOnline));
    }

    private int executeOnline(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");

        return 0;
    }

    private int executeProfile(CommandContext<ISender> context) {
        ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");

        return 0;
    }
}
