package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlinePlayerArgumentType;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MLinkCommand {
    private final CommandHandler handler;
    private final Map<GameProfile, Entry> gameProfileEntryMap = new ConcurrentHashMap<>();

    public MLinkCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literal) {
        return literal
                .then(handler.literal("to")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LINK_TO))
                        .then(handler.argument("player", OnlinePlayerArgumentType.players())
                                .executes(this::executeLinkTo)))
                .then(handler.literal("accept")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LINK_ACCEPT))
                        .then(handler.argument("name", StringArgumentType.string())
                                .executes(this::executeLinkAccept)))
                .then(handler.literal("code")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LINK_CODE))
                        .then(handler.argument("player", OnlinePlayerArgumentType.players())
                                .then(handler.argument("code", StringArgumentType.string())
                                        .executes(this::executeLinkCode))));
    }

    @SneakyThrows
    private int executeLinkCode(CommandContext<ISender> context) {
        GameProfile self = handler.requireDataCacheArgumentSelf(context).getValue1();
        IPlayer target = OnlinePlayerArgumentType.getPlayer(context, "player");
        String code = StringArgumentType.getString(context, "code");

        gameProfileEntryMap.values().removeIf(e -> e.timeMills < System.currentTimeMillis() - 30000);
        Entry entry = gameProfileEntryMap.get(self);
        if(entry == null || !entry.receiverUserInGameUUID.equals(target.getUniqueId()) || entry.code == null){
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_invalid"));
            return 0;
        }
        if(!entry.code.equals(code)){
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_invalid_code"));
            return 0;
        }
        gameProfileEntryMap.remove(self);

        CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(entry.requesterOnlineProfile.getValue1().getId(),
                entry.requesterOnlineProfile.getValue2(), entry.receiverUserInGameUUID);

        context.getSource().sendMessagePL(
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_succeed",
                        new Pair<>("redirect_name", target.getName()),
                        new Pair<>("redirect_uuid", target.getUniqueId())
                ));

        context.getSource().getAsPlayer().kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_kickmessage",
                new Pair<>("redirect_name", target.getName()),
                new Pair<>("redirect_uuid", target.getUniqueId())
        ));
        return 0;
    }

    private int executeLinkAccept(CommandContext<ISender> context) throws CommandSyntaxException {
        handler.requireDataCacheArgumentSelf(context);
        String string = StringArgumentType.getString(context, "name");
        gameProfileEntryMap.values().removeIf(e -> e.timeMills < System.currentTimeMillis() - CommandHandler.getCore().getPluginConfig().getLinkAcceptValidTimeMills());
        Optional<Map.Entry<GameProfile, Entry>> entry = gameProfileEntryMap.entrySet().stream()
                .filter(e -> e.getKey().getName().equalsIgnoreCase(string))
                .filter(e -> e.getValue().receiverUserInGameUUID.equals(context.getSource().getAsPlayer().getUniqueId()))
                .filter(e -> e.getValue().code == null).findFirst();

        if (entry.isEmpty()) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_accept_invalid"));
            return 0;
        }
        Map.Entry<GameProfile, Entry> profileEntryEntry = entry.get();

        String targetServiceName;
        BaseServiceConfig bsc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profileEntryEntry.getValue().requesterOnlineProfile.getValue2());
        if (bsc == null) {
            targetServiceName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_unidentified_name");
        } else {
            targetServiceName = bsc.getName();
        }

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            profileEntryEntry.getValue().code = ValueUtil.generateLinkCode();
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_accept",
                    new Pair<>("code", profileEntryEntry.getValue().code),
                    new Pair<>("profile_name", context.getSource().getAsPlayer().getName())
            ));
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_accept_desc",
                new Pair<>("target_service_name", targetServiceName),
                new Pair<>("target_service_id", profileEntryEntry.getValue().requesterOnlineProfile.getValue2()),
                new Pair<>("target_online_name", profileEntryEntry.getKey().getName()),
                new Pair<>("target_online_uuid", profileEntryEntry.getKey().getId()),
                new Pair<>("profile_name", context.getSource().getAsPlayer().getName()),
                new Pair<>("profile_uuid", context.getSource().getAsPlayer().getUniqueId())
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_accept_cq",
                new Pair<>("target_service_name", targetServiceName),
                new Pair<>("target_service_id", profileEntryEntry.getValue().requesterOnlineProfile.getValue2()),
                new Pair<>("target_online_name", profileEntryEntry.getKey().getName()),
                new Pair<>("target_online_uuid", profileEntryEntry.getKey().getId()),
                new Pair<>("profile_name", context.getSource().getAsPlayer().getName()),
                new Pair<>("profile_uuid", context.getSource().getAsPlayer().getUniqueId())
        ));

        return 0;
    }

    private int executeLinkTo(CommandContext<ISender> context) throws CommandSyntaxException {
        Pair<GameProfile, Integer> self = handler.requireDataCacheArgumentSelf(context);
        IPlayer target = OnlinePlayerArgumentType.getPlayer(context, "player");

        handler.requirePlayerAndNoSelf(context, target);
        handler.requireDataCacheArgumentOther(target);

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            gameProfileEntryMap.put(self.getValue1(), new Entry(self, target.getUniqueId()));
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_link",
                    new Pair<>("self_online_name", self.getValue1().getName())
            ));
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_link_to_desc",
                new Pair<>("redirect_name", target.getName()),
                new Pair<>("redirect_uuid", target.getUniqueId())
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_link_to_cq",
                new Pair<>("redirect_name", target.getName()),
                new Pair<>("redirect_uuid", target.getUniqueId())
        ));
        return 0;
    }

    public static class Entry {
        // 请求发起时间
        private final long timeMills = System.currentTimeMillis();
        // 请求发起玩家的在线档案
        private final Pair<GameProfile, Integer> requesterOnlineProfile;
        // 请求发起玩家请求的档案uuid
        private final UUID receiverUserInGameUUID;
        // 连接码
        private String code;

        public Entry(Pair<GameProfile, Integer> requesterOnlineProfile, UUID receiverUserInGameUUID) {
            this.requesterOnlineProfile = requesterOnlineProfile;
            this.receiverUserInGameUUID = receiverUserInGameUUID;
        }
    }
}
