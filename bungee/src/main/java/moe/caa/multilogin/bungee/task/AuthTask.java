package moe.caa.multilogin.bungee.task;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.ReflectUtil;
import moe.caa.multilogin.core.auth.AuthErrorEnum;
import moe.caa.multilogin.core.auth.AuthResult;
import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.auth.VerificationResult;
import moe.caa.multilogin.core.data.data.PluginData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class AuthTask implements Runnable {
    private static Field LOGIN_PROFILE;
    private static Field NAME;
    private static Field UNIQUE_ID;
    private static Method FINISH;
    InitialHandler handler;
    String arg;
    public AuthTask(InitialHandler handler, String arg) {
        this.handler = handler;
        this.arg = arg;
    }

    public static void init() throws NoSuchFieldException {
        Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
        LOGIN_PROFILE = ReflectUtil.getField(INITIAL_HANDLE_CLASS, LoginResult.class);
        NAME = ReflectUtil.getField(INITIAL_HANDLE_CLASS, "name");
        UNIQUE_ID = ReflectUtil.getField(INITIAL_HANDLE_CLASS, "uniqueId");
        FINISH = ReflectUtil.getMethod(INITIAL_HANDLE_CLASS, "finish");
    }

    @Override
    public void run() {
        try {
            AuthResult<LoginResult> result = HttpAuth.yggAuth(handler.getName(), arg, BungeeCord.getInstance().gson, LoginResult.class);
            if (result.getErr() != null) {
                if (result.getErr() == AuthErrorEnum.SERVER_DOWN) {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
                } else {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("offline_mode_player"));
                }
                return;
            }
            LoginResult loginResult = result.getResult();
            UUID onlineId = Util.getUUID(loginResult.getId());
            VerificationResult verificationResult = MultiCore.getUserVerificationMessage(onlineId, handler.getName(), result.getYggdrasilService());
            if (verificationResult.getFAIL_MSG() != null) {
                handler.disconnect(new TextComponent(verificationResult.getFAIL_MSG()));
                return;
            }
            LOGIN_PROFILE.set(handler, new MultiLoginSignLoginResult(loginResult));
            UNIQUE_ID.set(handler, verificationResult.getREDIRECT_UUID());
            NAME.set(handler, loginResult.getName());
            FINISH.invoke(handler);
        } catch (Exception e) {
            e.printStackTrace();
            handler.disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
            MultiCore.getPlugin().getPluginLogger().severe("处理用户数据时出现异常");
        }
    }
}
