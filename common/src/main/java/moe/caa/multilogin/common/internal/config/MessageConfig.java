package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.NodePath;

public class MessageConfig extends Configuration {
    public final ConfigurationValue<Component> loginUnknownError = miniMsg(NodePath.path("login-unknown-error"));
    public final ConfigurationValue<Component> loginHelloPacketInvalidCharactersInName = miniMsg(NodePath.path("login-hello-packet-invalid-characters-in-name"));

    public final ConfigurationValue<Component> loginProfileCreateUuidConflict = miniMsg(NodePath.path("login-profile-create-uuid-conflict"));
    public final ConfigurationValue<Component> loginProfileCreateNameConflict = miniMsg(NodePath.path("login-profile-create-name-conflict"));
    public final ConfigurationValue<Component> loginProfileCreateNameAmendRestrict = miniMsg(NodePath.path("login-profile-create-name-amend-restrict"));

    public final ConfigurationValue<Component> loginFailedRemoteAuthenticationOnly = miniMsg(NodePath.path("login-failed-remote-authentication-only"));
    public final ConfigurationValue<Component> loginFailedLocalAuthenticationInvalidSession = miniMsg(NodePath.path("login-failed-local-authentication-invalid-session"));
    public final ConfigurationValue<Component> loginFailedLocalAuthenticationUnavailable = miniMsg(NodePath.path("login-failed-local-authentication-unavailable"));

    public final ConfigurationValue<Component> loginFailedRemoteAuthenticationNotCarryCookie = miniMsg(NodePath.path("login-failed-remote-authentication-not-carry-cookie"));

    public final ConfigurationValue<Component> commandRequiredPlayer = miniMsg(NodePath.path("command-required-player"));
    public final ConfigurationValue<Component> commandDescriptionHelp = miniMsg(NodePath.path("command-description-help"));
    public final ConfigurationValue<Component> commandHelpNone = miniMsg(NodePath.path("command-help-none"));
    public final ConfigurationValue<Component> commandHelpHeader = miniMsg(NodePath.path("command-help-header"));
    public final ConfigurationValue<Component> commandHelpEntry = miniMsg(NodePath.path("command-help-entry"));

    public final ConfigurationValue<Component> commandArgumentOnlinePlayerNotFound = miniMsg(NodePath.path("command-argument-online-player-not-found"));

    public final ConfigurationValue<Component> commandDescriptionMe = miniMsg(NodePath.path("command-description-me"));
    public final ConfigurationValue<Component> commandMeNotFoundOnlineData = miniMsg(NodePath.path("command-me-not-found-online-data"));
    public final ConfigurationValue<Component> commandMeContent = miniMsg(NodePath.path("command-me-content"));
    public final ConfigurationValue<Component> commandDescriptionInfo = miniMsg(NodePath.path("command-description-info"));
    public final ConfigurationValue<Component> commandInfoNotFoundOnlineData = miniMsg(NodePath.path("command-info-not-found-online-data"));
    public final ConfigurationValue<Component> commandInfoContent = miniMsg(NodePath.path("command-info-content"));


    public final ConfigurationValue<Component> commandDescriptionCreate = miniMsg(NodePath.path("command-description-create"));
    public final ConfigurationValue<Component> commandCreateNotFoundOnlineData = miniMsg(NodePath.path("command-Create-not-found-online-data"));
    public final ConfigurationValue<Component> commandCreateMaxLimited = miniMsg(NodePath.path("command-create-max-limited"));
    public final ConfigurationValue<Component> commandCreateFailedNameContainSpace = miniMsg(NodePath.path("command-create-failed-name-contain-space"));
    public final ConfigurationValue<Component> commandCreateFailedNameTooLong = miniMsg(NodePath.path("command-create-failed-name-too-long"));
    public final ConfigurationValue<Component> commandCreateFailedNameNoMatchesRegular = miniMsg(NodePath.path("command-create-failed-name-no-matches-regular"));
    public final ConfigurationValue<Component> commandCreateFailedNameConflict = miniMsg(NodePath.path("command-create-failed-name-conflict"));
    public final ConfigurationValue<Component> commandCreateFailedThrow = miniMsg(NodePath.path("command-create-failed-throw"));
    public final ConfigurationValue<Component> commandCreateSucceed = miniMsg(NodePath.path("command-create-succeed"));


}
