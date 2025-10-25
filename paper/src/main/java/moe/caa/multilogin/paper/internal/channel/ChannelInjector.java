package moe.caa.multilogin.paper.internal.channel;

import io.netty.channel.Channel;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelInjector implements ChannelInitializeListener {
    public static final String MINECRAFT_PACKET_HANDLER_NAME = "packet_handler";
    private final MultiLoginPaperMain multiLoginPaperMain;
    private final Key key = Key.key("multilogin:main");

    public ChannelInjector(MultiLoginPaperMain multiLoginPaperMain) {
        this.multiLoginPaperMain = multiLoginPaperMain;
    }

    public void inject() throws Exception {
        LoginPhasePacketHandler.init();
        ChannelInitializeListenerHolder.addListener(Key.key("multilogin:main"), this);
    }

    public void uninject() {
        ChannelInitializeListenerHolder.removeListener(key);
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        channel.pipeline().addBefore(MINECRAFT_PACKET_HANDLER_NAME, "multilogin_login_phase_packet_handler", new LoginPhasePacketHandler(multiLoginPaperMain));
    }
}
