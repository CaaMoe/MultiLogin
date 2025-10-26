package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import moe.caa.multilogin.common.internal.util.EditableMiniMessage;
import org.spongepowered.configurate.NodePath;

public class MessageConfig extends Configuration {
    public final ConfigurationValue<EditableMiniMessage> loginUnknownError = editableMiniMsg(NodePath.path("login-unknown-error"));
    public final ConfigurationValue<EditableMiniMessage> loginHelloPacketInvalidCharactersInName = editableMiniMsg(NodePath.path("login-hello-packet-invalid-characters-in-name"));

    public final ConfigurationValue<EditableMiniMessage> loginProfileCreateUuidConflict = editableMiniMsg(NodePath.path("login-profile-create-uuid-conflict"));
    public final ConfigurationValue<EditableMiniMessage> loginProfileCreateNameConflict = editableMiniMsg(NodePath.path("login-profile-create-name-conflict"));
    public final ConfigurationValue<EditableMiniMessage> loginProfileCreateNameAmendRestrict = editableMiniMsg(NodePath.path("login-profile-create-name-amend-restrict"));

    public final ConfigurationValue<EditableMiniMessage> loginFailedLocalAuthenticationInvalidSession = editableMiniMsg(NodePath.path("login-failed-local-authentication-invalid-session"));
    public final ConfigurationValue<EditableMiniMessage> loginFailedLocalAuthenticationUnavailable = editableMiniMsg(NodePath.path("login-failed-local-authentication-unavailable"));

    public final ConfigurationValue<EditableMiniMessage> loginFailedDirectAuthenticationOnlyUseTransferLogin = editableMiniMsg(NodePath.path("login-failed-direct-authentication-only-use-transfer-login"));
    public final ConfigurationValue<EditableMiniMessage> loginFailedTransferAuthenticationNotCarryCookie = editableMiniMsg(NodePath.path("login-failed-transfer-authentication-not-carry-cookie"));
    public final ConfigurationValue<EditableMiniMessage> loginFailedTransferAuthenticationCarryCookieHasExpired = editableMiniMsg(NodePath.path("login-failed-transfer-authentication-carry-cookie-has-expired"));
    public final ConfigurationValue<EditableMiniMessage> loginFailedTransferAuthenticationCarryCookieInvalidSignature = editableMiniMsg(NodePath.path("login-failed-transfer-authentication-carry-cookie-invalid-signature"));

    public final ConfigurationValue<EditableMiniMessage> commandGeneralError = editableMiniMsg(NodePath.path("command-general-error"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralRequiredPlayer = editableMiniMsg(NodePath.path("command-general-required-player"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralNotFoundOnlineDataMe = editableMiniMsg(NodePath.path("command-general-not-found-online-data-me"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralNotFoundOnlineDataTarget = editableMiniMsg(NodePath.path("command-general-not-found-online-data-target"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralNotFoundSlotProfileMe = editableMiniMsg(NodePath.path("command-general-not-found-slot-profile-me"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralReconnectFeatureNotEnabled = editableMiniMsg(NodePath.path("command-general-reconnect-feature-not-enabled"));
    public final ConfigurationValue<EditableMiniMessage> commandGeneralArgumentOnlinePlayerNotFound = editableMiniMsg(NodePath.path("command-general-argument-online-player-not-found"));


    public final ConfigurationValue<EditableMiniMessage> commandDescriptionHelp = editableMiniMsg(NodePath.path("command-description-help"));
    public final ConfigurationValue<EditableMiniMessage> commandHelpNone = editableMiniMsg(NodePath.path("command-help-none"));
    public final ConfigurationValue<EditableMiniMessage> commandHelpHeader = editableMiniMsg(NodePath.path("command-help-header"));
    public final ConfigurationValue<EditableMiniMessage> commandHelpEntry = editableMiniMsg(NodePath.path("command-help-entry"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionMe = editableMiniMsg(NodePath.path("command-description-me"));
    public final ConfigurationValue<EditableMiniMessage> commandMeContent = editableMiniMsg(NodePath.path("command-me-content"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionInfo = editableMiniMsg(NodePath.path("command-description-info"));
    public final ConfigurationValue<EditableMiniMessage> commandInfoContent = editableMiniMsg(NodePath.path("command-info-content"));


    public final ConfigurationValue<EditableMiniMessage> commandDescriptionCreate = editableMiniMsg(NodePath.path("command-description-create"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateMaxLimited = editableMiniMsg(NodePath.path("command-create-max-limited"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateFailedNameContainSpace = editableMiniMsg(NodePath.path("command-create-failed-name-contain-space"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateFailedNameTooLong = editableMiniMsg(NodePath.path("command-create-failed-name-too-long"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateFailedNameNoMatchesRegular = editableMiniMsg(NodePath.path("command-create-failed-name-no-matches-regular"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateFailedNameConflict = editableMiniMsg(NodePath.path("command-create-failed-name-conflict"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateFailedThrow = editableMiniMsg(NodePath.path("command-create-failed-throw"));
    public final ConfigurationValue<EditableMiniMessage> commandCreateSucceed = editableMiniMsg(NodePath.path("command-create-succeed"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionProfiles = editableMiniMsg(NodePath.path("command-description-profiles"));
    public final ConfigurationValue<EditableMiniMessage> commandProfilesHeader = editableMiniMsg(NodePath.path("command-profiles-header"));
    public final ConfigurationValue<EditableMiniMessage> commandProfilesProfileEntry = editableMiniMsg(NodePath.path("command-profiles-profile-entry"));
    public final ConfigurationValue<EditableMiniMessage> commandProfilesDefaultProfileEntry = editableMiniMsg(NodePath.path("command-profiles-default-profile-entry"));
    public final ConfigurationValue<EditableMiniMessage> commandProfilesFooter = editableMiniMsg(NodePath.path("command-profiles-footer"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionProfileLogin = editableMiniMsg(NodePath.path("command-description-profile-login"));
    public final ConfigurationValue<EditableMiniMessage> commandProfileLoginFailedAlready = editableMiniMsg(NodePath.path("command-profile-login-failed-already"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionProfileSetDefault = editableMiniMsg(NodePath.path("command-description-profile-set-default"));
    public final ConfigurationValue<EditableMiniMessage> commandProfileSetDefaultFailedAlready = editableMiniMsg(NodePath.path("command-profile-set-default-failed-already"));
    public final ConfigurationValue<EditableMiniMessage> commandProfileSetDefaultSucceed = editableMiniMsg(NodePath.path("command-profile-set-default-succeed"));

    public final ConfigurationValue<EditableMiniMessage> commandDescriptionAdminReload = editableMiniMsg(NodePath.path("command-description-admin-reload"));
    public final ConfigurationValue<EditableMiniMessage> commandAdminReloadSucceed = editableMiniMsg(NodePath.path("command-admin-reload-succeed"));


}
