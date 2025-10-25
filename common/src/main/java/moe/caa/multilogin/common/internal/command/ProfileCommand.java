package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.common.internal.data.Profile;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.data.cookie.ReconnectSpecifiedProfileIDCookieData;
import moe.caa.multilogin.common.internal.data.cookie.SignedCookieData;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.manager.CommandManager;

import java.util.Map;
import java.util.function.Consumer;

public class ProfileCommand<S> extends SubCommand<S> {
    public ProfileCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionProfiles = "multilogin.command.profiles";
        addCommandDescription("profiles", permissionProfiles, manager.core.messageConfig.commandDescriptionProfiles.get());

        builder.then(literal("profiles")
                .requires(predicateHasPermission(permissionProfiles))
                .executes(context -> {
                    manager.executeAsync(() -> profiles(context.getSource()));
                    return Command.SINGLE_SUCCESS;
                })
        );

        String permissionProfileLogin = "multilogin.command.profile.login";
        String permissionProfileSetDefault = "multilogin.command.profile.setdefault";

        addCommandDescription("profile <slot_id> login", permissionProfileLogin, manager.core.messageConfig.commandDescriptionProfileLogin.get());
        addCommandDescription("profile <slot_id> setdefault", permissionProfileSetDefault, manager.core.messageConfig.commandDescriptionProfileSetDefault.get());


        builder.then(literal("profile").then(argument("slot_id", IntegerArgumentType.integer(0))
                .then(literal("login")
                        .requires(predicateHasPermission(permissionProfileLogin))
                        .executes(context -> {
                            manager.executeAsync(() -> profileLogin(context));
                            return Command.SINGLE_SUCCESS;
                        })

                ).then(literal("setdefault")
                        .requires(predicateHasPermission(permissionProfileSetDefault))
                        .executes(context -> {
                            manager.executeAsync(() -> profileSetDefault(context));
                            return Command.SINGLE_SUCCESS;
                        })
                )));
    }

    private void ifFetchedProfileRunOrElseTip(Sender sender, int userID, int slotID, Consumer<Profile> consumer) {
        Profile slotProfile = manager.core.databaseHandler.getProfileByOwnerIDAndSlotID(userID, slotID);
        if (slotProfile == null) {
            sender.sendMessage(manager.core.messageConfig.commandGeneralNotFoundSlotProfileMe.get()
                    .replace("<slot_id>", String.valueOf(slotID))
                    .build()
            );
            return;
        }
        consumer.accept(slotProfile);
    }

    private void profileSetDefault(CommandContext<S> context) {
        Sender sender = manager.wrapSender(context.getSource());
        ifOnlinePlayerRunOrElseTip(sender, player -> {
            ifFetchMeOnlineDataRunOrElseTip(sender, player, onlineData -> {
                int slotId = IntegerArgumentType.getInteger(context, "slot_id");
                ifFetchedProfileRunOrElseTip(sender, onlineData.onlineUser().userID(), slotId, slotProfile -> {
                    Integer currentSelectProfileSlot = manager.core.databaseHandler.getUserCurrentSelectProfileSlot(onlineData.onlineUser().userID());
                    if (currentSelectProfileSlot != null && currentSelectProfileSlot == slotId) {
                        player.sendMessage(manager.core.messageConfig.commandProfileSetDefaultFailedAlready.get()
                                .replace("<slot_id>", String.valueOf(slotId))
                                .replace("<profile_name>", slotProfile.profileName)
                                .build()
                        );
                    } else {
                        manager.core.databaseHandler.updateUserCurrentSelectProfileSlot(onlineData.onlineUser().userID(), slotId);
                        player.sendMessage(manager.core.messageConfig.commandProfileSetDefaultSucceed.get()
                                .replace("<slot_id>", String.valueOf(slotId))
                                .replace("<profile_name>", slotProfile.profileName)
                                .build()
                        );
                    }
                });
            });
        });
    }

    private void profileLogin(CommandContext<S> context) {
        Sender sender = manager.wrapSender(context.getSource());
        ifOnlinePlayerRunOrElseTip(sender, player -> {
            ifFetchMeOnlineDataRunOrElseTip(sender, player, onlineData -> {
                int slotId = IntegerArgumentType.getInteger(context, "slot_id");
                ifFetchedProfileRunOrElseTip(sender, onlineData.onlineUser().userID(), slotId, slotProfile -> {
                    if (onlineData.onlineProfile().profileSlotID() == slotId) {
                        player.sendMessage(manager.core.messageConfig.commandProfileLoginFailedAlready.get()
                                .replace("<slot_id>", String.valueOf(slotId))
                                .replace("<profile_name>", slotProfile.profileName)
                                .build()
                        );
                        return;
                    }
                    ifReconnectFeatureEnableRunOrElseTip(sender, () -> {
                        ReconnectSpecifiedProfileIDCookieData cookieData = new ReconnectSpecifiedProfileIDCookieData();
                        cookieData.userID = onlineData.onlineUser().userID();
                        cookieData.specifiedProfileID = slotProfile.profileID;
                        cookieData.authenticatedGameProfile = onlineData.onlineUser().authenticatedGameProfile();

                        cookieData.setRelativelyExpireSecond(10);

                        SignedCookieData<?> signedCookieData;
                        try {
                            signedCookieData = SignedCookieData.signData(
                                    cookieData,
                                    manager.core.mainConfig.localRsa.get().privateKey.get(),
                                    manager.core.mainConfig.localRsa.get().verifyDigitalSignatureAlgorithm.get()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        player.writeCookie(MultiCore.COOKIE_KEY, signedCookieData.toBytes());
                        player.transfer(manager.core.mainConfig.reconnectFeature.get().reconnectAddress.get().orElseGet(player::getConnectedServerAddress));
                    });
                });
            });
        });
    }

    private void profiles(S source) {
        Sender sender = manager.wrapSender(source);
        ifOnlinePlayerRunOrElseTip(sender, player -> {
            ifFetchMeOnlineDataRunOrElseTip(sender, player, onlineData -> {

                Map<Integer, Profile> profiles = manager.core.databaseHandler.getProfilesByOwnerID(onlineData.onlineUser().userID());
                Integer selectProfileSlot = manager.core.databaseHandler.getUserCurrentSelectProfileSlot(onlineData.onlineUser().userID());


                player.sendMessage(
                        manager.core.messageConfig.commandProfilesHeader.get()
                                .replace("<current_profile_count>", String.valueOf(profiles.size()))
                                .replace("<max_profile_count>", String.valueOf(CreateCommand.calcMaxSlotCount(manager, player)))
                                .build()
                );

                for (Map.Entry<Integer, Profile> entry : profiles.entrySet()) {
                    int slot = entry.getKey();
                    Profile profile = entry.getValue();

                    if (selectProfileSlot != null && selectProfileSlot == slot) {
                        player.sendMessage(manager.core.messageConfig.commandProfilesDefaultProfileEntry.get()
                                .replace("<slot_id>", String.valueOf(slot))
                                .replace("<profile_name>", profile.profileName)
                                .build()
                        );
                    } else {
                        player.sendMessage(manager.core.messageConfig.commandProfilesProfileEntry.get()
                                .replace("<slot_id>", String.valueOf(slot))
                                .replace("<profile_name>", profile.profileName)
                                .build()
                        );
                    }
                }

                player.sendMessage(
                        manager.core.messageConfig.commandProfilesFooter.get()
                                .replace("<current_profile_count>", String.valueOf(profiles.size()))
                                .replace("<max_profile_count>", String.valueOf(CreateCommand.calcMaxSlotCount(manager, player)))
                                .build()
                );
            });

        });
    }
}
