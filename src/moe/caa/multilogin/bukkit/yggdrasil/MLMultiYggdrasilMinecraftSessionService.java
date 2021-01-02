package moe.caa.multilogin.bukkit.yggdrasil;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.bukkit.PluginData;
import moe.caa.multilogin.bukkit.MultiLogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.FutureTask;

public class MLMultiYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private MinecraftSessionService vanService;
    private final URL CHECK_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");

    protected MLMultiYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) throws NoSuchFieldException {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String s, String s1) throws AuthenticationException {
        vanService.joinServer(gameProfile, s, s1);
    }

    public GameProfile hasJoinedServer(GameProfile gameProfile, String s) throws AuthenticationUnavailableException {
        return hasJoinedServer(gameProfile, s, null);
    }

    public GameProfile hasJoinedServer(GameProfile gameProfile, String s, InetAddress inetAddress) throws AuthenticationUnavailableException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", gameProfile.getName());
        arguments.put("serverId", s);
        if (inetAddress != null) {
            arguments.put("ip", inetAddress.getHostAddress());
        }

        URL url;
        url = HttpAuthenticationService.concatenateURL(CHECK_URL, HttpAuthenticationService.buildQuery(arguments));

        try {
            MLHasJoinedMinecraftServerResponse response = ((MLMultiYggdrasilAuthenticationService) this.getAuthenticationService()).makeRequest(url, null, MLHasJoinedMinecraftServerResponse.class);
            if (response != null && response.getId() != null) {
                UUID swap = PluginData.getSwapUUID(response.getId(), response.getYggService(), gameProfile.getName());
                GameProfile result = new MLGameProfile(response.getId(), swap, gameProfile.getName(), response.getYggService());
                if (response.getProperties() != null) {
                    result.getProperties().putAll(response.getProperties());
                }

                // 在验证通过此处踢掉恶意抢注ID者
                FutureTask<Object> task = new FutureTask<>(() -> {
                    if (PluginData.isNoRepeatedName() && response.getYggService().getPath().equalsIgnoreCase(PluginData.getSafeIdService())) {
                        String name = gameProfile.getName();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().equalsIgnoreCase(name)) {
                                player.kickPlayer(PluginData.getConfigurationConfig().getString("msgRushNameOnl"));
                            }
                        }
                    }
                    return null;
                });
                Bukkit.getScheduler().runTask(MultiLogin.INSTANCE, task);
                try {
                    task.get();
                } catch (Exception ignored) {
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

    public void setVanService(MinecraftSessionService vanService) {
        this.vanService = vanService;
    }
}
