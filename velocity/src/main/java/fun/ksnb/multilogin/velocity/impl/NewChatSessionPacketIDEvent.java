package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
public class NewChatSessionPacketIDEvent {
    private final int packetID;
    private final ProtocolVersion version;
    private final Player player;
}
