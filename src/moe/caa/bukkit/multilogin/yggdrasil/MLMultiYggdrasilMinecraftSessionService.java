package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import moe.caa.bukkit.multilogin.PluginData;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MLMultiYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private MinecraftSessionService vanService;
    private final Class YggdrasilMinecraftSessionServiceClass = YggdrasilMinecraftSessionService.class;
    private final Field field = YggdrasilMinecraftSessionServiceClass.getDeclaredField("checkUrl");
    private URL checkUrl;

    protected MLMultiYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) throws NoSuchFieldException {
        super(authenticationService);
        field.setAccessible(true);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String s, String s1) throws AuthenticationException {
        vanService.joinServer(gameProfile, s, s1);
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile gameProfile, String s, InetAddress inetAddress) throws AuthenticationUnavailableException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", gameProfile.getName());
        arguments.put("serverId", s);
        if (inetAddress != null) {
            arguments.put("ip", inetAddress.getHostAddress());
        }

        URL url;
        url = HttpAuthenticationService.concatenateURL(checkUrl, HttpAuthenticationService.buildQuery(arguments));

        try {
            MLHasJoinedMinecraftServerResponse response = ((MLMultiYggdrasilAuthenticationService) this.getAuthenticationService()).makeRequest(url, null, MLHasJoinedMinecraftServerResponse.class);
            System.out.println(response);
            if (response != null && response.getId() != null) {
                UUID swap = PluginData.getSwapUUID(response.getId(), response.getYggService(), gameProfile.getName());
                GameProfile result = new MLGameProfile(swap, gameProfile.getName(), response.getYggService());
                if (response.getProperties() != null) {
                    result.getProperties().putAll(response.getProperties());
                }

                return result;
            } else {
                return null;
            }
        } catch (AuthenticationUnavailableException var8) {
            throw var8;
        } catch (AuthenticationException var9) {
            return null;
        }
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
        this.checkUrl = (URL) field.get(vanService);
    }
}
