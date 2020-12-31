package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import java.net.Proxy;

public class MLMultiYggdrasilAuthenticationService extends HttpAuthenticationService {
    private YggdrasilAuthenticationService vanService;

    public MLMultiYggdrasilAuthenticationService() {
        super(Proxy.NO_PROXY);
    }

    @Override
    public UserAuthentication createUserAuthentication(Agent agent) {
        return vanService.createUserAuthentication(agent);
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        try {
            return new MLMultiYggdrasilMinecraftSessionService(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public GameProfileRepository createProfileRepository() {
        return vanService.createProfileRepository();
    }

    public void setVanService(YggdrasilAuthenticationService vanService) {
        this.vanService = vanService;
    }
}
