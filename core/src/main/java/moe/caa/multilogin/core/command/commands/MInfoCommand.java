package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlinePlayerArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.util.HashSet;
import java.util.Set;

public class MInfoCommand {
    private final CommandHandler handler;

    public MInfoCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.argument("player", OnlinePlayerArgumentType.players())
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CURRENT_OTHER))
                        .executes(this::executeInfo))
                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CURRENT_ONESELF))
                .executes(this::executeInfoOneself);
    }

    private int executeInfo(CommandContext<ISender> context) {
        Set<IPlayer> players = OnlinePlayerArgumentType.getPlayers(context, "player");
        processInfoCommand(context, players);
        return 0;
    }

    private int executeInfoOneself(CommandContext<ISender> context) throws CommandSyntaxException {
        handler.requirePlayer(context);
        IPlayer player = context.getSource().getAsPlayer();
        HashSet<IPlayer> players = new HashSet<>();
        players.add(player);
        processInfoCommand(context, players);
        return 0;
    }

    private void processInfoCommand(CommandContext<ISender> context, Set<IPlayer> players) {
        if (players.size() > 1) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage(
                    "command_message_info_multi", new Pair<>("size", players.size())));
        }

        for (IPlayer player : players) {
            Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
            if (profile == null) {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_unknown",
                        new Pair<>("name", player.getName()),
                        new Pair<>("uuid", player.getUniqueId())
                ));
            } else {
                String serviceName;
                BaseServiceConfig bsc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profile.getValue2());
                if (bsc == null) {
                    serviceName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_unidentified_name");
                } else {
                    serviceName = bsc.getName();
                }
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info",
                        new Pair<>("name", player.getName()),
                        new Pair<>("uuid", player.getUniqueId()),
                        new Pair<>("service_name", serviceName),
                        new Pair<>("service_id", profile.getValue2()),
                        new Pair<>("online_name", profile.getValue1().getName()),
                        new Pair<>("online_uuid", profile.getValue1().getId())
                ));
            }
        }
    }
}
