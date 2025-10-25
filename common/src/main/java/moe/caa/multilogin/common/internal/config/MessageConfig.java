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
    public final ConfigurationValue<Component> commandMeNone = miniMsg(NodePath.path("command-me-none"));
    public final ConfigurationValue<Component> commandMeContent = miniMsg(NodePath.path("command-me-content"));
    public final ConfigurationValue<Component> commandDescriptionInfo = miniMsg(NodePath.path("command-description-info"));
    public final ConfigurationValue<Component> commandInfoNone = miniMsg(NodePath.path("command-info-none"));
    public final ConfigurationValue<Component> commandInfoContent = miniMsg(NodePath.path("command-info-content"));
}
