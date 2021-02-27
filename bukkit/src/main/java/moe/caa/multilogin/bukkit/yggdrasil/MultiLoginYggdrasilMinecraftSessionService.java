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
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.ReflectUtil;
import moe.caa.multilogin.core.auth.AuthErrorEnum;
import moe.caa.multilogin.core.auth.AuthResult;
import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.auth.VerificationResult;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private MinecraftSessionService vanService;
    private final URL CHECK_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");
    private final Field yggdrasilAuthenticationServiceGson = ReflectUtil.getField(YggdrasilAuthenticationService.class, Gson.class);
    private Gson gson;


    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String s, String s1) throws AuthenticationException {
        vanService.joinServer(gameProfile, s, s1);
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        URL url = HttpAuthenticationService.concatenateURL(CHECK_URL, HttpAuthenticationService.buildQuery(arguments));

        try {
            String arg = null;
            for(String s : url.toString().split("/")){
                if(s.startsWith("hasJoined?")){
                    arg = s;
                }
            }

            AuthResult<HasJoinedMinecraftServerResponse> authResult = HttpAuth.yggAuth(user.getName(), arg, gson, HasJoinedMinecraftServerResponse.class);
            HasJoinedMinecraftServerResponse response = authResult.getResult();
            if(authResult.getErr() == AuthErrorEnum.SERVER_DOWN){
                throw new AuthenticationUnavailableException();
            }

            if (response != null && response.getId() != null) {

                VerificationResult verificationResult = MultiCore.getUserVerificationMessage(response.getId(), user.getName(), authResult.getYggdrasilService());
                if(verificationResult.getFAIL_MSG() != null){
                    BukkitListener.AUTH_CACHE.put(Thread.currentThread(), verificationResult.getFAIL_MSG());
                    return new GameProfile(response.getId(), user.getName());
                }

                GameProfile result = new GameProfile(verificationResult.getREDIRECT_UUID(), user.getName());
                if (response.getProperties() != null) {
                    result.getProperties().putAll(response.getProperties());
                }

                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe("处理用户数据时出现异常");
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
