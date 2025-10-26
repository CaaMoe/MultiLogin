package moe.caa.multilogin.common.internal.handler;

import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig;
import moe.caa.multilogin.common.internal.config.authentication.LocalAuthenticationConfig;
import moe.caa.multilogin.common.internal.config.authentication.RemoteAuthenticationConfig;
import moe.caa.multilogin.common.internal.data.GameProfile;
import moe.caa.multilogin.common.internal.data.LoggingUser;
import moe.caa.multilogin.common.internal.data.Profile;
import moe.caa.multilogin.common.internal.data.User;
import moe.caa.multilogin.common.internal.data.cookie.ReconnectCookieData;
import moe.caa.multilogin.common.internal.data.cookie.ReconnectSpecifiedProfileIDCookieData;
import moe.caa.multilogin.common.internal.data.cookie.RemoteAuthenticatedCookieData;
import moe.caa.multilogin.common.internal.data.cookie.SignedCookieData;
import moe.caa.multilogin.common.internal.main.MultiCore;

import java.security.PublicKey;

public final class TransferLoginHandler extends LoginHandler {
    public TransferLoginHandler(MultiCore core) {
        super(core);
    }

    void handleTransferLogin(LoggingUser loggingUser) throws Throwable {
        core.platform.getPlatformLogger().debug("Start handle the login(transfer) of " + loggingUser.getExpectUsername());

        // 拿到 Cookie
        byte[] cookieDataBytes = loggingUser.requestCookie(MultiCore.COOKIE_KEY);
        if (cookieDataBytes == null || cookieDataBytes.length == 0) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but did not carry a valid cookie.");
            loggingUser.closeConnect(core.messageConfig.loginFailedTransferAuthenticationNotCarryCookie.get().build());
            return;
        }

        // Cookie 过期了
        SignedCookieData<?> signedCookieData = SignedCookieData.readSignedCookieData(cookieDataBytes);
        if (signedCookieData.cookieData().hasExpired()) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the cookie(" + signedCookieData.cookieData().getDescription() + ") carried has expired.");
            loggingUser.closeConnect(core.messageConfig.loginFailedTransferAuthenticationCarryCookieHasExpired.get().build());
            return;
        }

        // 处理 Cookie, 在这里没有检查 Cookie 的签名!
        switch (signedCookieData.cookieData()) {
            case ReconnectSpecifiedProfileIDCookieData cookieData ->
                    handleReconnectSpecifiedProfileLogin(loggingUser, signedCookieData, cookieData);
            case ReconnectCookieData cookieData -> handleReconnectLogin(loggingUser, signedCookieData, cookieData);
            case RemoteAuthenticatedCookieData cookieData ->
                    handleRemoteAuthenticatedLogin(loggingUser, signedCookieData, cookieData);
        }
    }

    private void handleRemoteAuthenticatedLogin(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, RemoteAuthenticatedCookieData cookieData) throws Throwable {
        String serviceID = cookieData.serviceID;
        AuthenticationConfig authenticationConfig = core.authenticationServiceMap.get(serviceID);
        if (!(authenticationConfig instanceof RemoteAuthenticationConfig remoteAuthenticationConfig)) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the specified remote authentication service was not found. (remote authentication service: " + cookieData.serviceID + ")");
            loggingUser.closeConnect(core.messageConfig.loginFailedRemoteAuthenticationNotFoundService.get());
            return;
        }

        if (!validateSignatureOrCloseConnect(loggingUser, signedCookieData,
                remoteAuthenticationConfig.remoteRSAPublicKey.get(),
                remoteAuthenticationConfig.remoteRSAVerifyDigitalSignatureAlgorithm.get()
        )) {
            return;
        }


        core.platform.getPlatformLogger().debug("Start handle the remote login(" + cookieData.getDescription() + ") of " + loggingUser.getExpectUsername());

        switch (loggingUser.switchToEncryptedState(false)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult ->
                    handleSwitchToEncryptedFailedResult(loggingUser, failedResult);
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult ignored ->
                    autoSelectProfileLogin(loggingUser, remoteAuthenticationConfig,
                            core.databaseHandler.updateOrCreateUser(remoteAuthenticationConfig,
                                    cookieData.authenticatedGameProfile), cookieData.authenticatedGameProfile
                    );
        }
    }


    public boolean validateSignatureOrCloseConnect(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, PublicKey publicKey, String algorithm) throws Exception {
        if (!signedCookieData.validateSignature(
                publicKey,
                algorithm
        )) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the signature of the cookie(" + signedCookieData.cookieData().getDescription() + ") carried was invalid.");
            loggingUser.closeConnect(core.messageConfig.loginFailedTransferAuthenticationCarryCookieInvalidSignature.get().build());
            return false;
        }
        return true;
    }

    private void handleReconnectSpecifiedProfileLogin(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, ReconnectSpecifiedProfileIDCookieData cookieData) throws Throwable {
        if (!validateSignatureOrCloseConnect(loggingUser, signedCookieData,
                core.mainConfig.localRsa.get().publicKey.get(),
                core.mainConfig.localRsa.get().verifyDigitalSignatureAlgorithm.get()
        )) {
            return;
        }

        core.platform.getPlatformLogger().debug("Start handle the transfer login(" + cookieData.getDescription() + ") of " + loggingUser.getExpectUsername());

        int profileID = cookieData.specifiedProfileID;
        int userID = cookieData.userID;
        GameProfile gameProfile = cookieData.authenticatedGameProfile;

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the server only allowed transfer login(remote authentication only).");
        }

        User user = core.databaseHandler.getUserByUserID(userID);
        Profile profile = core.databaseHandler.getProfileByProfileID(profileID);

        if (profile == null || user == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the specified user or profile did not exist.");
        }

        // 完成登录
        switch (loggingUser.switchToEncryptedState(false)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult ->
                    handleSwitchToEncryptedFailedResult(loggingUser, failedResult);
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult ignored ->
                    specifiedProfileLogin(loggingUser, core.localAuthenticationConfig, user, gameProfile, profile);
        }
    }

    private void handleReconnectLogin(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, ReconnectCookieData cookieData) throws Throwable {
        if (!validateSignatureOrCloseConnect(loggingUser, signedCookieData,
                core.mainConfig.localRsa.get().publicKey.get(),
                core.mainConfig.localRsa.get().verifyDigitalSignatureAlgorithm.get()
        )) {
            return;
        }


        core.platform.getPlatformLogger().debug("Start handle the transfer login(" + cookieData.getDescription() + ") of " + loggingUser.getExpectUsername());

        int userID = cookieData.userID;
        GameProfile gameProfile = cookieData.authenticatedGameProfile;

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the server only allowed transfer login(remote authentication only).");
        }

        User user = core.databaseHandler.getUserByUserID(userID);

        if (user == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the specified user did not exist.");
        }


        switch (loggingUser.switchToEncryptedState(false)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult ->
                    handleSwitchToEncryptedFailedResult(loggingUser, failedResult);
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult ignored ->
                    autoSelectProfileLogin(loggingUser, localAuthenticationConfig, user, gameProfile);
        }
    }
}
