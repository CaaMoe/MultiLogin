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
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.auth.*;
import moe.caa.multilogin.core.data.data.UserTextures;
import moe.caa.multilogin.core.skin.SkinRepairHandler;
import moe.caa.multilogin.core.util.I18n;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private final Field yggdrasilAuthenticationServiceGson = ReflectUtil.getField(YggdrasilAuthenticationService.class, Gson.class);
    private MinecraftSessionService vanService;

    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String authenticationToken, String serverId) throws AuthenticationException {
        vanService.joinServer(gameProfile, authenticationToken, serverId);
    }

    // Do not add Override annotation !
    public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException {
        return hasJoinedServer(user, serverId, null);
    }

    // Do not add Override annotation !
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
//        if (address != null) {
//            arguments.put("ip", address.getHostAddress());
//        }

        try {
//            验证阶段
            AuthResult<HasJoinedMinecraftServerResponse> authResult = HttpAuth.yggAuth(user.getName(), arguments);
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

            PropertyMap propertyMap = response.getProperties();
            if (propertyMap != null) {
                try {
                    AtomicReference<UserTextures> userProperty = new AtomicReference<>();
                    for (Map.Entry<String, Property> entry : propertyMap.entries()) {
                        if (entry.getKey().equals("textures")) {
                            if (userProperty.get() == null) {
                                userProperty.set(SkinRepairHandler.repairThirdPartySkin(response.getId(), entry.getValue().getValue(), entry.getValue().getSignature(), authResult.getYggdrasilService()));
                            }
                        }
                    }

                    result.getProperties().clear();
                    result.getProperties().put("textures", new Property("textures", userProperty.get().getRepair_property().getValue(), userProperty.get().getRepair_property().getSignature()));


                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.severe(I18n.getTransString("plugin_error_skin_repair", user.getName()));
                }
            }

            MultiLoginBukkit.LOGIN_CACHE.remove(verificationResult.getREDIRECT_UUID());
            MultiLoginBukkit.LOGIN_CACHE.put(verificationResult.getREDIRECT_UUID(), System.currentTimeMillis());

            return result;

        } catch (AuthenticationUnavailableException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_io_user"));
        }
        return null;
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile gameProfile, boolean requireSecure) {
        return vanService.getTextures(gameProfile, requireSecure);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile gameProfile, boolean requireSecure) {
        return vanService.fillProfileProperties(gameProfile, requireSecure);
    }

    public void setVanService(MinecraftSessionService vanService) throws IllegalAccessException {
        this.vanService = vanService;
        AuthTask.setServicePair(HasJoinedMinecraftServerResponse.class, (Gson) yggdrasilAuthenticationServiceGson.get(this.getAuthenticationService()));
    }
}
