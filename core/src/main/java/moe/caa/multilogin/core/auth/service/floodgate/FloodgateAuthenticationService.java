package moe.caa.multilogin.core.auth.service.floodgate;

import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.auth.LoginAuthResult;
import moe.caa.multilogin.core.auth.service.yggdrasil.UnmodifiableGameProfile;
import moe.caa.multilogin.core.configuration.service.FloodgateServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import org.geysermc.floodgate.api.InstanceHolder;
import org.geysermc.floodgate.api.handshake.HandshakeData;
import org.geysermc.floodgate.api.handshake.HandshakeHandler;
import org.geysermc.floodgate.util.BedrockData;
import org.geysermc.floodgate.util.LinkedPlayer;

import java.util.HashMap;
import java.util.UUID;

public class FloodgateAuthenticationService implements HandshakeHandler {
    private final MultiCore multiCore;

    public FloodgateAuthenticationService(MultiCore multiCore) {
        this.multiCore = multiCore;
    }

    public void register() {
        InstanceHolder.getHandshakeHandlers().addHandshakeHandler(this);
    }

    @Override
    public void handle(HandshakeData handshakeData) {
        if(!multiCore.getPluginConfig().isFloodgateSupport()){
            return;
        }
        BedrockData data = handshakeData.getBedrockData();
        String xuid = data.getXuid();
        UUID uuid = ValueUtil.xuidToUUID(xuid);
        GameProfile profile = new UnmodifiableGameProfile(uuid, initBedrockUsername(handshakeData.getBedrockData().getUsername()), new HashMap<>());
        FloodgateServiceConfig service = multiCore.getPluginConfig().getFloodgateAuthenticationService();
        if (service == null) {
            handshakeData.setDisconnectReason(multiCore.getLanguageHandler().getMessage("auth_floodgate_service_notfound"));
            return;
        }
        FloodgateAuthenticationResult result = new FloodgateAuthenticationResult(profile, service);
        LoginAuthResult loginAuthResult = multiCore.getAuthHandler().checkIn(result);
        if (loginAuthResult.getResult() == AuthResult.Result.ALLOW) {
            GameProfile gameProfile = loginAuthResult.getResponse();
            handshakeData.setLinkedPlayer(
                    LinkedPlayer.of(
                            gameProfile.getName(), gameProfile.getId()
                            , uuid)
            );
        } else {
            handshakeData.setDisconnectReason(loginAuthResult.getKickMessage());
        }
    }

    private String initBedrockUsername(String bedrockUsername) {
        char[] charArray = bedrockUsername.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : charArray) {
            stringBuilder.append(isNameAllowedCharacter(c) ? c : '_');
        }
        return stringBuilder.toString();
    }

    private boolean isNameAllowedCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_';
    }
}
