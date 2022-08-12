package moe.caa.multilogin.velocity.injector.redirect;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerChat;
import io.netty.buffer.ByteBuf;

public class MultiPlayerChat extends PlayerChat {
    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
