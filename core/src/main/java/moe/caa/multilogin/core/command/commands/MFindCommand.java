package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.There;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlineArgumentType;
import moe.caa.multilogin.core.command.argument.ProfileArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MFindCommand {
    private final CommandHandler handler;

    public MFindCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(
                        handler.literal("profile")
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_FIND_PROFILE))
                                .then(handler.argument("profile", ProfileArgumentType.profile())
                                        .executes(this::executeProfile)))
                .then(handler.literal("online")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_FIND_ONLINE))
                        .then(handler.argument("online", OnlineArgumentType.online())
                                .executes(this::executeOnline)));
    }

    @SneakyThrows
    private int executeOnline(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
        String whitelist = online.isWhitelist() ?
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_whitelist_true") :
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_whitelist_false");
        UUID profileUUID = online.getProfileUUID();
        if (profileUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online",
                    new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                    new Pair<>("service_id", online.getBaseServiceConfig().getId()),
                    new Pair<>("online_uuid", online.getOnlineUUID()),
                    new Pair<>("online_name", online.getOnlineName()),
                    new Pair<>("whitelist", whitelist),
                    new Pair<>("profile", CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_profilenotexist"))
            ));
            return 0;
        }

        String profileName = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getUsername(profileUUID);
        if (profileName == null) {
            profileName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_profileunnamed");
        }
        String profileInfo = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_profile",
                new Pair<>("profile_uuid", profileUUID),
                new Pair<>("profile_name", profileName)
        );
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online",
                new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                new Pair<>("service_id", online.getBaseServiceConfig().getId()),
                new Pair<>("online_uuid", online.getOnlineUUID()),
                new Pair<>("online_name", online.getOnlineName()),
                new Pair<>("whitelist", whitelist),
                new Pair<>("profile", profileInfo)
        ));


        return 0;
    }

    @SneakyThrows
    private int executeProfile(CommandContext<ISender> context) {
        ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
        UUID profileUUID = profile.getProfileUUID();
        Set<There<UUID, String, Integer>> onlineProfiles = CommandHandler.getCore().getSqlManager().getUserDataTable().getOnlineProfiles(profileUUID);
        String profileName = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getUsername(profileUUID);
        if (profileName == null) {
            profileName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_unnamed");
        }
        String message = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile",
                new Pair<>("profile_uuid", profileUUID),
                new Pair<>("profile_name", profileName),
                new Pair<>("count", onlineProfiles.size()),
                new Pair<>("list", onlineProfiles.stream().map(p -> {
                    BaseServiceConfig serviceConfig = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(p.getValue3());
                    String serviceName = serviceConfig == null ? CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_unused_service") : serviceConfig.getName();
                    return CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry",
                            new Pair<>("service_name", serviceName),
                            new Pair<>("service_id", p.getValue3()),
                            new Pair<>("online_uuid", p.getValue1()),
                            new Pair<>("online_name", Optional.ofNullable(p.getValue2()).orElse(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_onlineunnamed")))
                    );
                }).collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_delimiter"))))
        );
        context.getSource().sendMessagePL(message);
        return 0;
    }
}
