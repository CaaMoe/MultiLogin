package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.data.Profile;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import moe.caa.multilogin.common.internal.manager.ProfileManager;
import net.kyori.adventure.text.TextReplacementConfig;

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
                        .executes(context -> {
                            manager.executeAsync(() -> create(context));
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private void create(CommandContext<S> context) {
        ifOnlinePlayerRunOrElseTip(context.getSource(), player -> {
            OnlineData onlineData = player.getOnlineData();
            if (onlineData == null) {
                player.sendMessage(manager.core.messageConfig.commandCreateNotFoundOnlineData.get());
                return;
            }
            int userID = onlineData.onlineUser().userID();

            Map<Integer, Profile> currentHaveProfiles = manager.core.databaseHandler.getProfilesByOwnerID(userID);
            int maxSlotCount = calcMaxSlotCount(player);

            if (maxSlotCount <= currentHaveProfiles.size()) {
                player.sendMessage(manager.core.messageConfig.commandCreateMaxLimited.get().replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<max>")
                                .replacement(String.valueOf(maxSlotCount))
                                .build()
                ));
                return;
            }

            String name = StringArgumentType.getString(context, "name");
            if (name.contains(" ")) {
                player.sendMessage(manager.core.messageConfig.commandCreateFailedNameContainSpace.get().replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<name>")
                                .replacement(name)
                                .build()
                ));
                return;
            }

            if (name.length() > 16) {
                player.sendMessage(manager.core.messageConfig.commandCreateFailedNameTooLong.get().replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<name>")
                                .replacement(name)
                                .build()
                ));
                return;
            }

            if (!Pattern.matches(manager.core.mainConfig.userProfileSlotCountLimit.get().profileNameRegularExpressionRestriction.get(), name)) {
                player.sendMessage(manager.core.messageConfig.commandCreateFailedNameNoMatchesRegular.get()
                        .replaceText(TextReplacementConfig.builder()
                                .matchLiteral("<name>")
                                .replacement(name)
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .matchLiteral("<regular>")
                                .replacement(manager.core.mainConfig.userProfileSlotCountLimit.get().profileNameRegularExpressionRestriction.get())
                                .build()
                        ));
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
                            player.sendMessage(manager.core.messageConfig.commandCreateFailedNameConflict.get().replaceText(
                                    TextReplacementConfig.builder()
                                            .matchLiteral("<name>")
                                            .replacement(name)
                                            .build()
                            ));
                        }
                        case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult -> {
                            manager.core.platform.getPlatformLogger().error("Failed to create profile during user creation.", throwResult.throwable);
                            player.sendMessage(manager.core.messageConfig.commandCreateFailedThrow.get().replaceText(
                                    TextReplacementConfig.builder()
                                            .matchLiteral("<name>")
                                            .replacement(name)
                                            .build()
                            ));
                        }
                    }
                    ;
                }
                case ProfileManager.CreateProfileResult.CreateProfileSucceedResult succeed -> {
                    player.sendMessage(manager.core.messageConfig.commandCreateSucceed.get()
                            .replaceText(TextReplacementConfig.builder()
                                    .matchLiteral("<name>")
                                    .replacement(name)
                                    .build())
                            .replaceText(TextReplacementConfig.builder()
                                    .matchLiteral("<slot>")
                                    .replacement(String.valueOf(chooseNextSlotID))
                                    .build())
                    );
                }
            }
        });
    }

    private int calcMaxSlotCount(OnlinePlayer player) {
        for (Map.Entry<String, Integer> entry : manager.core.mainConfig.userProfileSlotCountLimit.get().permissionMaxSlotCounts.get().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return Math.max(1, entry.getValue());
            }
        }
        return manager.core.mainConfig.userProfileSlotCountLimit.get().defaultMaxSlotCount.get();
    }
}

