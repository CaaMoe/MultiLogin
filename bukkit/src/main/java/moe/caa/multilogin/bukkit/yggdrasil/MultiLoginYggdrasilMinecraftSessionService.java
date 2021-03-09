/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.yggdrasil.MultiLoginYggdrasilMinecraftSessionService
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.yggdrasil;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.auth.*;
import moe.caa.multilogin.core.util.I18n;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private final URL CHECK_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");
    private final Field yggdrasilAuthenticationServiceGson = ReflectUtil.getField(YggdrasilAuthenticationService.class, Gson.class);
    private MinecraftSessionService vanService;
    private Gson gson;

    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String s, String s1) throws AuthenticationException {
        vanService.joinServer(gameProfile, s, s1);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId) {
        return hasJoinedServer(user, serverId, null);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        try {
//            验证阶段
            AuthResult<HasJoinedMinecraftServerResponse> authResult = HttpAuth.yggAuth(user.getName(), arguments, gson, HasJoinedMinecraftServerResponse.class);
//            后处理
            HasJoinedMinecraftServerResponse response = authResult.getResult();
            if (authResult.getErr() == AuthErrorEnum.SERVER_DOWN) {
                throw new AuthenticationUnavailableException();
            }

            if (response == null || response.getId() == null) return null;
            VerificationResult verificationResult = Verifier.getUserVerificationMessage(response.getId(), user.getName(), authResult.getYggdrasilService());
            if (verificationResult.getFAIL_MSG() != null) {
                BukkitListener.AUTH_CACHE.put(Thread.currentThread(), verificationResult.getFAIL_MSG());
                return new GameProfile(response.getId(), user.getName());
            }

            GameProfile result = new GameProfile(verificationResult.getREDIRECT_UUID(), user.getName());
            if (response.getProperties() != null) {
                result.getProperties().putAll(response.getProperties());
            }

            MultiLoginBukkit.LOGIN_CACHE.remove(verificationResult.getREDIRECT_UUID());
            MultiLoginBukkit.LOGIN_CACHE.put(verificationResult.getREDIRECT_UUID(), System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_io_user"));
        }
        return null;
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile gameProfile, boolean b) {
        return vanService.getTextures(gameProfile, b);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile gameProfile, boolean b) {
        return vanService.fillProfileProperties(gameProfile, b);
    }

    public void setVanService(MinecraftSessionService vanService) throws IllegalAccessException {
        this.vanService = vanService;
        gson = (Gson) yggdrasilAuthenticationServiceGson.get(this.getAuthenticationService());
    }
}
