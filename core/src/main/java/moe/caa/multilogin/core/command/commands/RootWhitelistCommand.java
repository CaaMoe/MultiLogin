package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.arguments.StringArgumentType;
import moe.caa.multilogin.core.impl.CallbackTransmit;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;

public class RootWhitelistCommand extends BaseCommand {
    public RootWhitelistCommand(MultiCore core) {
        super(core);
    }

    @Override
    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                literal("whitelist")
                        .then(literal("add")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_ADD))
                                .then(argument("target", StringArgumentType.string())
                                        .executes(this::executeAdd)
                                )
                        )
                        .then(literal("remove")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_REMOVE))
                                .then(argument("target", StringArgumentType.string())
                                        .executes(this::executeRemove)
                                )
                        )
                        .then(literal("list")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_LIST))
                                .executes(this::executeList)
                        )
                        .then(literal("clearCache")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_CLEAR_CACHE))
                                .executes(this::executeClearCache)
                        )
        );
    }

    private int executeClearCache(CommandContext<ISender> context) {
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("confirm").content(getCore().getLanguageHandler().getMessage("command_message_whitelist_cache_clear_confirm", FormatContent.empty())).build()
        )));

        CallbackTransmit<Void> cb = value -> {
            int i = getCore().getSqlManager().getCacheWhitelistDataHandler().removeAllCacheWhitelist();
            if (i == 0) {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_cache_clear_empty", FormatContent.empty()));
            } else {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_cache_cleared", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("count").content(i).build()
                )));
            }

        };

        getSecondaryConfirmationHandler().submit(context.getSource(), cb);
        return 0;
    }

    @SneakyThrows
    private int executeAdd(CommandContext<ISender> context) {
        String nameOrUuid = StringArgumentType.getString(context, "target");
        if (getCore().getSqlManager().getCacheWhitelistDataHandler().addCacheWhitelist(nameOrUuid)) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_added", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(nameOrUuid).build()
            )));
        } else {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(nameOrUuid).build()
            )));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        String nameOrUuid = StringArgumentType.getString(context, "target");
        int count = 0;
        if (getCore().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(nameOrUuid)) {
            count++;
            getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(nameOrUuid,
                        getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
            });
        }
        for (User user : getCore().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(nameOrUuid)) {
            if (user.isWhitelist()) {
                user.setWhitelist(false);
                getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
                count++;
                getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                    getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(user.getRedirectUuid(),
                            getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                });
            }
        }
        UUID whenUuid = ValueUtil.getUuidOrNull(nameOrUuid);
        if (whenUuid != null) {
            for (User user : getCore().getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(whenUuid)) {
                if (user.isWhitelist()) {
                    user.setWhitelist(false);
                    getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
                    count++;
                    getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                        getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(user.getRedirectUuid(),
                                getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                    });
                }
            }
            User whenOnlineUuidUser = getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(whenUuid);
            if (whenOnlineUuidUser != null) {
                if (whenOnlineUuidUser.isWhitelist()) {
                    whenOnlineUuidUser.setWhitelist(false);
                    getCore().getSqlManager().getUserDataHandler().updateUserEntry(whenOnlineUuidUser);
                    count++;
                    getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                        getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(whenOnlineUuidUser.getRedirectUuid(),
                                getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                    });
                }
            }
        }
        if (count != 0) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_removed", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(nameOrUuid).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            )));
        } else {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(nameOrUuid).build()
            )));
        }
        return 0;
    }

    @SneakyThrows
    private int executeList(CommandContext<ISender> context) {
        List<User> users = getCore().getSqlManager().getUserDataHandler().getUserEntryWhereHaveWhitelist();
        List<String> cacheWhitelist = getCore().getSqlManager().getCacheWhitelistDataHandler().getAllCacheWhitelist();
        int count = users.size() + cacheWhitelist.size();
        if (count == 0) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_list_empty", FormatContent.empty()));
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : cacheWhitelist) {
            sb.append("§7*").append(s).append("*").append(", ");
        }
        if (cacheWhitelist.size() != 0 && users.size() == 0) sb.setLength(sb.length() - 2);
        for (User user : users) {
            FutureTask<Boolean> task = new FutureTask<>(() -> getCore().getPlugin().getRunServer().getPlayerManager().hasOnline(user.getRedirectUuid()));
            getCore().getPlugin().getRunServer().getScheduler().runTask(task);
            boolean b = task.get();
            sb.append('§').append(b ? 'a' : '7');
            sb.append(user.getCurrentName()).append(", ");
        }
        if (users.size() != 0) sb.setLength(sb.length() - 2);
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_list", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("count").content(count).build(),
                FormatContent.FormatEntry.builder().name("cache_count").content(cacheWhitelist.size()).build(),
                FormatContent.FormatEntry.builder().name("list").content(sb).build()
        )));
        return 0;
    }
}
