package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.data.Profile;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import moe.caa.multilogin.common.internal.manager.ProfileManager;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class CreateCommand<S> extends SubCommand<S> {
    public CreateCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionCreate = "multilogin.command.create";
        addCommandDescription("create <name>", permissionCreate, manager.core.messageConfig.commandDescriptionCreate.get());

        builder.then(literal("create")
                .requires(predicateHasPermission(permissionCreate))
                .then(argument("name", StringArgumentType.string())
                        .executes(context ->
                                manager.executeAsync(context, () -> create(context))
                        )));
    }

    protected static <S> int calcMaxSlotCount(CommandManager<S> manager, OnlinePlayer player) {
        for (Map.Entry<String, Integer> entry : manager.core.mainConfig.userProfileSlotCountLimit.get().permissionMaxSlotCounts.get().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return Math.max(1, entry.getValue());
            }
        }
        return manager.core.mainConfig.userProfileSlotCountLimit.get().defaultMaxSlotCount.get();
    }

    private void create(CommandContext<S> context) {
        Sender sender = manager.wrapSender(context.getSource());
        String name = StringArgumentType.getString(context, "name");

        ifOnlinePlayerRunOrElseTip(sender, player -> {
            try {
                OnlineData onlineData = player.getOnlineData();
                if (onlineData == null) {
                    player.sendMessage(manager.core.messageConfig.commandGeneralNotFoundOnlineDataMe.get().build());
                    return;
                }
                int userID = onlineData.onlineUser().userID();

                Map<Integer, Profile> currentHaveProfiles = manager.core.databaseHandler.getProfilesByOwnerID(userID);
                int maxSlotCount = calcMaxSlotCount(manager, player);

                if (maxSlotCount <= currentHaveProfiles.size()) {
                    player.sendMessage(manager.core.messageConfig.commandCreateMaxLimited.get()
                            .replace("<max>", String.valueOf(maxSlotCount))
                            .build()
                    );
                    return;
                }

                if (name.contains(" ")) {
                    player.sendMessage(manager.core.messageConfig.commandCreateFailedNameContainSpace.get()
                            .replace("<name>", name)
                            .build()
                    );
                    return;
                }

                if (name.length() > 16) {
                    player.sendMessage(manager.core.messageConfig.commandCreateFailedNameTooLong.get()
                            .replace("<name>", name)
                            .build()
                    );
                    return;
                }

                if (!Pattern.matches(manager.core.mainConfig.userProfileSlotCountLimit.get().profileNameRegularExpressionRestriction.get(), name)) {
                    player.sendMessage(manager.core.messageConfig.commandCreateFailedNameNoMatchesRegular.get()
                            .replace("<name>", name)
                            .replace("<regular>", manager.core.mainConfig.userProfileSlotCountLimit.get().profileNameRegularExpressionRestriction.get())
                            .build()
                    );
                    return;
                }

                int chooseNextSlotID = 0;
                while (currentHaveProfiles.containsKey(chooseNextSlotID)) {
                    chooseNextSlotID++;
                }

                switch (manager.core.profileManager.createProfile(
                        onlineData.onlineUser().userID(),
                        ProfileManager.UUIDConflictPolicy.RANDOM,
                        ProfileManager.NameConflictPolicy.REJECT,
                        UUID.randomUUID(),
                        name,
                        chooseNextSlotID
                )) {
                    case ProfileManager.CreateProfileResult.CreateProfileFailedResult createProfileFailedResult -> {
                        switch (createProfileFailedResult) {
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult enumResult -> {
                                player.sendMessage(manager.core.messageConfig.commandCreateFailedNameConflict.get()
                                        .replace("<name>", name)
                                        .build()
                                );
                            }
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult -> {
                                manager.core.platform.getPlatformLogger().error("Failed to create profile during user creation.", throwResult.throwable);
                                player.sendMessage(manager.core.messageConfig.commandCreateFailedThrow.get()
                                        .replace("<name>", name)
                                        .build()
                                );
                            }
                        }
                        ;
                    }
                    case ProfileManager.CreateProfileResult.CreateProfileSucceedResult succeed -> {
                        player.sendMessage(manager.core.messageConfig.commandCreateSucceed.get()
                                .replace("<name>", name)
                                .replace("<slot>", String.valueOf(chooseNextSlotID))
                                .build()
                        );
                    }
                }
            } catch (Throwable t) {
                manager.core.platform.getPlatformLogger().error("Failed to create profile during user creation.", t);
                player.sendMessage(manager.core.messageConfig.commandCreateFailedThrow.get()
                        .replace("<name>", name)
                        .build()
                );
            }
        });
    }
}

