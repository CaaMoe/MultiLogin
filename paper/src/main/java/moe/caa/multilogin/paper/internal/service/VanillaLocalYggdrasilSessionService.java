package moe.caa.multilogin.paper.internal.service;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;
import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import org.bukkit.craftbukkit.CraftServer;

import java.net.InetAddress;

public class VanillaLocalYggdrasilSessionService implements LocalYggdrasilSessionService {
    private final MultiLoginPaperMain paperMain;

    public VanillaLocalYggdrasilSessionService(MultiLoginPaperMain paperMain) {
        this.paperMain = paperMain;
    }

    @Override
    public HasJoinedResult hasJoined(String serverID, String username, InetAddress playerIP) {
        try {
            ProfileResult profileResult = ((CraftServer) paperMain.getServer()).getServer().getSessionService().hasJoinedServer(
                    username, serverID, playerIP
            );
            if (profileResult == null) {
                return new HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedInvalidSessionResult();
            }
            GameProfile gameProfile = profileResult.profile();
            moe.caa.multilogin.common.internal.data.GameProfile profile = new moe.caa.multilogin.common.internal.data.GameProfile(
                    gameProfile.getId(),
                    gameProfile.getName(),
                    gameProfile.getProperties().entries().stream().map(it -> new moe.caa.multilogin.common.internal.data.GameProfile.Property(
                            it.getValue().name(),
                            it.getValue().value(),
                            it.getValue().signature()
                    )).toList()
            );
            return new HasJoinedResult.HasJoinedSucceedResult(profile);
        } catch (AuthenticationUnavailableException e) {
            return new HasJoinedResult.HasJoinedFailedResult.hasJoinedFailedServiceUnavailableResult(e);
        } catch (Throwable t) {
            return new HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedThrowResult(t);
        }
    }
}
