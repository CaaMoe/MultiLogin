package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

public class ChatSessionHandler extends ChannelDuplexHandler {
    private final Player player;
    private final EventManager eventManager;
    public ChatSessionHandler(Player player, EventManager eventManager) {
        this.player = player;
        this.eventManager = eventManager;
    }

    @Override
    public void channelRead(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object packet
    ) throws Exception {
        if (packet instanceof ByteBuf buffer) {
            ByteBuf c = buffer.asReadOnly();
            c.markReaderIndex();
            try {
                int packetId = c.readByte();
                ProtocolUtils.readUuid(c);
                ProtocolUtils.readPlayerKey(player.getProtocolVersion(), c);
                eventManager.fire(new NewChatSessionPacketIDEvent(packetId,player.getProtocolVersion(),player));
            } catch (Throwable ignore) { } finally {
                c.resetReaderIndex();
            }
        }
        super.channelRead(ctx, packet);
    }
}
