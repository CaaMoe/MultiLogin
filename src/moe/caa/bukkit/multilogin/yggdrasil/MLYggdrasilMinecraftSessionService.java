package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import moe.caa.bukkit.multilogin.PluginData;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MLYggdrasilMinecraftSessionService extends YggdrasilMinecraftSessionService {
    private static Field checkUrl;

    static {
        try {
            checkUrl = YggdrasilMinecraftSessionService.class.getDeclaredField("checkUrl");
            checkUrl.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected MLYggdrasilMinecraftSessionService(YggdrasilAuthenticationService service, Environment env) {
        super(service, env);
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        URL url = null;
        try {
            url = HttpAuthenticationService.concatenateURL((URL) checkUrl.get(this), HttpAuthenticationService.buildQuery(arguments));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            MLHasJoinedMinecraftServerResponse response = ((MLYggdrasilAuthenticationService) this.getAuthenticationService()).makeRequest(url, null, MLHasJoinedMinecraftServerResponse.class);
            if (response != null && response.getId() != null) {
                UUID swap = PluginData.getSwapUUID(response.getId(), response.getYggService(), user.getName());
                GameProfile result = new MLGameProfile(swap, user.getName(), response.getYggService());
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
}
