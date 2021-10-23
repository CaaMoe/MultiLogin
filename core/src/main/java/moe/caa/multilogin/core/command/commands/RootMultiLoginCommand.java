package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.main.Version;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.GroupBurstArrayList;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RootMultiLoginCommand extends BaseCommand {
    public RootMultiLoginCommand(MultiCore core) {
        super(core);
    }

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                literal("multilogin")
                        .then(literal("reload")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                                .executes(this::executeReload)
                        )
                        .then(literal("update")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_UPDATE))
                                .executes(this::executeUpdate)
                        )
                        .then(literal("confirm")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                                .executes(this::executeConfirm)
                        )
                        .then(literal("list")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LIST))
                                .executes(this::executeList)
                        )
        );
    }

    @SneakyThrows
    private int executeList(CommandContext<ISender> context) {
        List<User> users = getCore().getSqlManager().getUserDataHandler().getAllUserEntryOrderYggdrasilService();
        getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
            GroupBurstArrayList<User> groupBurstArrayList = new GroupBurstArrayList<>();
            ArrayList<User> temp = new ArrayList<>();
            String currentYggdrasilPath = null;
            YggdrasilService currentYggdrasil = null;
            for (User user : users) {
                if (!getCore().getPlugin().getRunServer().getPlayerManager().hasOnline(user.getRedirectUuid()))
                    continue;
                if (!user.getYggdrasilService().equals(currentYggdrasilPath)) {
                    groupBurstArrayList.offer(temp);
                    temp = new ArrayList<>();
                    currentYggdrasilPath = user.getYggdrasilService();
                    currentYggdrasil = getCore().getYggdrasilServicesHandler().getYggdrasilService(currentYggdrasilPath);
                }
                user.setService(currentYggdrasil);
                temp.add(user);
            }
            groupBurstArrayList.offer(temp);
            int size = groupBurstArrayList.size();
            if (size == 0) {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_list_empty"));
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                while (groupBurstArrayList.hasNext()) {
                    ArrayList<User> next = groupBurstArrayList.next();
                    String list = next.stream().map(User::getCurrentName).map(s -> "§7" + s).collect(Collectors.joining(", "));
                    String nameOrPath = next.get(0).getService() == null ? next.get(0).getYggdrasilService() : next.get(0).getService().getName();
                    String pad = getCore().getLanguageHandler().getMessage("command_message_multilogin_entry", FormatContent.createContent(
                            FormatContent.FormatEntry.builder().name("name_or_path").content(nameOrPath).build(),
                            FormatContent.FormatEntry.builder().name("list").content(list).build()
                    ));
                    stringBuilder.append(pad);
                }
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_list", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("count").content(size).build(),
                        FormatContent.FormatEntry.builder().name("list_entries").content(stringBuilder.toString()).build()
                )));
            }
        });
        return 0;
    }

    @SneakyThrows
    private int executeConfirm(CommandContext<ISender> context) {
        if (!getSecondaryConfirmationHandler().confirm(context.getSource())) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation_unknown", FormatContent.empty()));
        }
        return 0;
    }

    private int executeUpdate(CommandContext<ISender> context) {
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_start", FormatContent.empty()));
        getCore().getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                if (getCore().getUpdater().shouldUpdate()) {
                    context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_need", FormatContent.createContent(
                            FormatContent.FormatEntry.builder().name("latest").content(getCore().getUpdater().latestVersion).build(),
                            FormatContent.FormatEntry.builder().name("current").content(Optional.ofNullable(getCore().getUpdater().currentVersion).map(Version::toString).orElse(getCore().getPlugin().getPluginVersion())).build()
                    )));
                } else {
                    context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_unwanted", FormatContent.empty()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_failed", FormatContent.empty()));
            }
        });
        return 0;
    }

    private int executeReload(CommandContext<ISender> context) {
        getCore().reload();
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_reloaded", FormatContent.empty()));
        return 0;
    }
}
