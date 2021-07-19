package moe.caa.multilogin.bungee.auth;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.auth.*;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class BungeeAuthTask implements Runnable {
    private static MethodHandle LOGIN_PROFILE;
    private static MethodHandle NAME;
    private static MethodHandle UNIQUE_ID;
    private static MethodHandle FINISH;
    private final InitialHandler handler;
    private final String USERNAME;
    private final String SERVER_ID;
    private final String IP;

    public BungeeAuthTask(InitialHandler handler, String username, String serverId, String ip) {
        this.handler = handler;
        USERNAME = username;
        SERVER_ID = serverId;
        IP = ip;
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Class<InitialHandler> INITIAL_HANDLER_CLASS = InitialHandler.class;

        LOGIN_PROFILE = ReflectUtil.super_lookup.unreflectSetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, LoginResult.class, false));
        NAME = ReflectUtil.super_lookup.unreflectSetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, "name", false));
        UNIQUE_ID = ReflectUtil.super_lookup.unreflectSetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, "uniqueId", false));
        FINISH = ReflectUtil.super_lookup.unreflect(ReflectUtil.getMethod(INITIAL_HANDLER_CLASS, "finish", false));
    }

    @Override
    public void run() {
        try {
            AuthResult<LoginResult> result = AuthCore.yggAuth(USERNAME, SERVER_ID, IP);
            if (result.err != null) {
                if (result.err == AuthFailedEnum.SERVER_DOWN) {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
                } else {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("offline_mode_player"));
                }
                return;
            }
            LoginResult loginResult = result.result;
            UUID onlineId = Util.getUUID(loginResult.getId());
            VerificationResult verificationResult = Verifier.getUserVerificationMessage(onlineId, handler.getName(), result.service);
            if (verificationResult.FAIL_MSG != null) {
                handler.disconnect(new TextComponent(verificationResult.FAIL_MSG));
                return;
            }

            LOGIN_PROFILE.invoke(handler, new MultiLoginSignLoginResult(loginResult));
            UNIQUE_ID.invoke(handler, verificationResult.REDIRECT_UUID);
            NAME.invoke(handler, loginResult.getName());
            FINISH.invoke(handler);
        } catch (Throwable e) {
            handler.disconnect(new TextComponent(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage()));
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.ERROR_AUTH.getMessage());
        }
    }
}