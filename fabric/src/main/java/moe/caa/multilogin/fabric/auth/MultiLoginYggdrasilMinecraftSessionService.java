package moe.caa.multilogin.fabric.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.Setter;
import moe.caa.multilogin.fabric.main.MultiLoginFabricPluginBootstrap;

import java.net.InetAddress;
import java.util.Map;

public class MultiLoginYggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    @Setter
    private HttpMinecraftSessionService vanService;

    @Setter
    private MultiLoginFabricPluginBootstrap bootstrap;

    public MultiLoginYggdrasilMinecraftSessionService(HttpAuthenticationService authenticationService) {
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
        FabricAuthCore authCore = new FabricAuthCore(bootstrap);
        return authCore.doAuth(user, serverId, address);
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
