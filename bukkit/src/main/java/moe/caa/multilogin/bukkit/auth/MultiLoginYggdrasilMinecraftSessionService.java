package moe.caa.multilogin.bukkit.auth;

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
import moe.caa.multilogin.bukkit.MultiLoginBukkit;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.core.auth.*;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private final Field yggdrasilAuthenticationServiceGson = ReflectUtil.getField(YggdrasilAuthenticationService.class, Gson.class, true);
    private MinecraftSessionService vanService;

    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) throws NoSuchFieldException {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        vanService.joinServer(profile, authenticationToken, serverId);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException {
        return hasJoinedServer(user, serverId, null);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        String ip = address == null ? null : address.getHostAddress();
        try {
            AuthResult<HasJoinedMinecraftServerResponse> authResult = AuthCore.yggAuth(user.getName(), serverId, ip);
            HasJoinedMinecraftServerResponse response = authResult.result;
            if (authResult.err == AuthFailedEnum.SERVER_DOWN) {
                throw new AuthenticationUnavailableException();
            }
            if (response == null || response.getId() == null) return null;
            VerificationResult verificationResult = Verifier.getUserVerificationMessage(response.getId(), user.getName(), authResult.service);
            if (verificationResult.FAIL_MSG != null) {
                BukkitListener.AUTH_CACHE.put(Thread.currentThread(), verificationResult.FAIL_MSG);
                return new GameProfile(response.getId(), user.getName());
            }

            GameProfile result = new GameProfile(verificationResult.REDIRECT_UUID, user.getName());

            MultiLoginBukkit.LOGIN_CACHE.remove(verificationResult.REDIRECT_UUID);
            MultiLoginBukkit.LOGIN_CACHE.put(verificationResult.REDIRECT_UUID, System.currentTimeMillis());
            MultiLoginBukkit.USER_CACHE.put(verificationResult.REDIRECT_UUID, verificationResult.USER);
            return result;
        } catch (AuthenticationUnavailableException e) {
            throw e;
        } catch (Exception e){
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.ERROR_AUTH.getMessage());
        }
        return null;
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        return vanService.getTextures(profile, requireSecure);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        return vanService.fillProfileProperties(profile, requireSecure);
    }

    public void setVanService(MinecraftSessionService vanService) throws IllegalAccessException {
        this.vanService = vanService;
        MultiLoginBukkit.authGson = (Gson) yggdrasilAuthenticationServiceGson.get(this.getAuthenticationService());
    }
}
