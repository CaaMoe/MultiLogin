/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.task.AuthTask
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.task;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.auth.*;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserTextures;
import moe.caa.multilogin.core.skin.SkinRepairHandler;
import moe.caa.multilogin.core.util.I18n;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class BungeeAuthTask implements Runnable {
    private static MethodHandle LOGIN_PROFILE;
    private static MethodHandle NAME;
    private static MethodHandle UNIQUE_ID;
    private static MethodHandle FINISH;
    InitialHandler handler;
    Map<String, String> arg;

    public BungeeAuthTask(InitialHandler handler, Map<String, String> arg) {
        this.handler = handler;
        this.arg = arg;
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException {
        Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        LOGIN_PROFILE = ReflectUtil.getFieldUnReflectSetter(INITIAL_HANDLE_CLASS, LoginResult.class);
        NAME = ReflectUtil.getFieldUnReflectSetter(INITIAL_HANDLE_CLASS, "name");
        UNIQUE_ID = ReflectUtil.getFieldUnReflectSetter(INITIAL_HANDLE_CLASS, "uniqueId");
        FINISH = lookup.unreflect(ReflectUtil.getMethod(INITIAL_HANDLE_CLASS, "finish"));
    }

    @Override
    public void run() {
        try {
            AuthResult<LoginResult> result = HttpAuth.yggAuth(handler.getName(), arg);
            if (result.getErr() != null) {
//                错误检查
                if (result.getErr() == AuthErrorEnum.SERVER_DOWN) {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
                } else {
                    handler.disconnect(BungeeCord.getInstance().getTranslation("offline_mode_player"));
                }
                return;
            }
            LoginResult loginResult = result.getResult();
            UUID onlineId = Util.getUUID(loginResult.getId());
            VerificationResult verificationResult = Verifier.getUserVerificationMessage(onlineId, handler.getName(), result.getYggdrasilService());
            if (verificationResult.getFAIL_MSG() != null) {
                handler.disconnect(new TextComponent(verificationResult.getFAIL_MSG()));
                return;
            }

            AtomicReference<UserTextures> userProperty = new AtomicReference<>();

            try {
                for(LoginResult.Property entry : loginResult.getProperties()){
                    if(entry.getName().equals("textures")){
                        if(userProperty.get() == null){
                            userProperty.set(SkinRepairHandler.repairThirdPartySkin(onlineId, entry.getValue(), entry.getSignature()));
                        }
                    }
                }

                loginResult.setProperties(new LoginResult.Property[]{new LoginResult.Property("textures", userProperty.get().getRepair_property().getValue(), userProperty.get().getRepair_property().getSignature())});
            } catch (Exception e){
                e.printStackTrace();
                MultiCore.severe(I18n.getTransString("plugin_error_skin_repair", handler.getName()));
            }

            LOGIN_PROFILE.invoke(handler, new MultiLoginSignLoginResult(loginResult));
            UNIQUE_ID.invoke(handler, verificationResult.getREDIRECT_UUID());
            NAME.invoke(handler, loginResult.getName());
            FINISH.invoke(handler);
        } catch (Throwable e) {
            e.printStackTrace();
            handler.disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
            MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_io_user"));
        }
    }
}
