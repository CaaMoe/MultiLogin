/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.auth.BungeeAuthTask
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.task;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import moe.caa.multilogin.core.auth.AuthFailedEnum;
import moe.caa.multilogin.core.auth.AuthResult;
import moe.caa.multilogin.core.auth.VerificationResult;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

/*
捕获到登入包后主要进行登入处理的线程 核心部分
 */
public class VelocityAuthTask implements Runnable {
    private static MethodHandle INITPLAYER;

    private final LoginSessionHandler loginSessionHandler;
    private final String username;
    private final String serverId;
    private final String ip;
    private final MultiCore core;
    private final InitialInboundConnection inbound;

    public VelocityAuthTask(LoginSessionHandler handler, String username, String serverId, String ip, MultiCore core, InitialInboundConnection inbound) {
        this.loginSessionHandler = handler;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.core = core;
        this.inbound = inbound;
    }

    public static void init() throws NoSuchMethodException, IllegalAccessException {
        INITPLAYER = ReflectUtil.super_lookup.unreflect(ReflectUtil.getMethod(LoginSessionHandler.class, "initializePlayer", true, GameProfile.class, boolean.class));
    }

    @Override
    public void run() {
        try {
            AuthResult<GameProfile> result = core.getAuthCore().yggAuth(username, serverId, ip);
//            登入结果返回 服务器连接层面失败
            if (core.getLogger().isDebug() && result.throwable != null) {
//                异常
                core.getLogger().log(LoggerLevel.ERROR, result.throwable);
            }

            if (result.err != null) {
                if (result.err == AuthFailedEnum.SERVER_DOWN) {
//                    inbound.disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
//                    等会的 找不到翻译 这是临时的
                    inbound.disconnect(Component.translatable("velocity.error.online-mode-only",
                            NamedTextColor.RED));
                } else {
                    inbound.disconnect(Component.translatable("velocity.error.online-mode-only",
                            NamedTextColor.RED));
                }
                return;
            }
//            成功连接且取得结果
            GameProfile gameProfile = result.result;
            UUID onlineId = gameProfile.getId();
            VerificationResult verificationResult = core.getVerifier().getUserVerificationMessage(onlineId, username, result.service);
//            白名单 重名验证层面失败
            if (verificationResult.FAIL_MSG != null) {
                inbound.disconnect(Component.text(verificationResult.FAIL_MSG));
                return;
            }
//            将信息传回到handler
//            成功
            INITPLAYER.invoke(loginSessionHandler, gameProfile, true);
        } catch (Throwable e) {
            inbound.disconnect(Component.text(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage()));
            core.getLogger().log(LoggerLevel.ERROR, e);
            core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.ERROR_AUTH.getMessage());
        }
    }
}