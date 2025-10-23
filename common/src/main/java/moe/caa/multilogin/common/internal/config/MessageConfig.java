package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.NodePath;

public class MessageConfig extends Configuration {
    public final ConfigurationValue<Component> loginUnknownError = miniMsg(NodePath.path("login-unknown-error"));
    public final ConfigurationValue<Component> loginInvalidCharactersInName = miniMsg(NodePath.path("login-invalid-characters-in-name"));

    public final ConfigurationValue<Component> loginProfileCreateUuidConflict = miniMsg(NodePath.path("login-profile-create-uuid-conflict"));
    public final ConfigurationValue<Component> loginProfileCreateNameConflict = miniMsg(NodePath.path("login-profile-create-name-conflict"));
    public final ConfigurationValue<Component> loginProfileCreateNameAmendRestrict = miniMsg(NodePath.path("login-profile-create-name-amend-restrict"));

    public final ConfigurationValue<Component> commandRequiredPlayer = miniMsg(NodePath.path("command-required-player"));
    public final ConfigurationValue<Component> commandDescriptionHelp = miniMsg(NodePath.path("command-description-help"));
    public final ConfigurationValue<Component> commandHelpNone = miniMsg(NodePath.path("command-help-none"));
    public final ConfigurationValue<Component> commandHelpHeader = miniMsg(NodePath.path("command-help-header"));
    public final ConfigurationValue<Component> commandHelpEntry = miniMsg(NodePath.path("command-help-entry"));

    public final ConfigurationValue<Component> commandArgumentOnlinePlayerNotFound = miniMsg(NodePath.path("command-argument-online-player-not-found"));

    public final ConfigurationValue<Component> commandDescriptionInfo = miniMsg(NodePath.path("command-description-info"));
    public final ConfigurationValue<Component> commandInfoNone = miniMsg(NodePath.path("command-info-none"));
    public final ConfigurationValue<Component> commandInfoContent = miniMsg(NodePath.path("command-info-content"));
    public final ConfigurationValue<Component> commandDescriptionInfoOther = miniMsg(NodePath.path("command-description-info-other"));
    public final ConfigurationValue<Component> commandInfoOtherNone = miniMsg(NodePath.path("command-info-other-none"));
    public final ConfigurationValue<Component> commandInfoOtherContent = miniMsg(NodePath.path("command-info-other-content"));
}
