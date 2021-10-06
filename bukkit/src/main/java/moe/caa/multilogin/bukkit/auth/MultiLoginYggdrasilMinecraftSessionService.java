package moe.caa.multilogin.bukkit.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.Setter;

import java.net.InetAddress;
import java.util.Map;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    @Setter
    private HttpMinecraftSessionService vanService;

    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        vanService.joinServer(profile, authenticationToken, serverId);
    }

    // 不要删不要删不要删不要删不要删不要删不要删不要删
    public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException {
        return hasJoinedServer(user, serverId, null);
    }

    // 不要注解不要注解不要注解不要注解不要注解不要注解
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        BukkitAuthCore core = new BukkitAuthCore();
        return core.doAuth(user, serverId, address);
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        return vanService.getTextures(profile, requireSecure);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        return vanService.fillProfileProperties(profile, requireSecure);
    }
}
