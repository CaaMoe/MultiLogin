package moe.caa.multilogin.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;

/**
 * 登录阶段玩家被动断开链接事件
 */
public class LoginStateOnKeyEvent {
    public static final Event<EventCallback> INSTANCE = EventFactory.createArrayBacked(EventCallback.class, callbacks -> eventData -> {
        for (EventCallback callback : callbacks) {
            callback.accept(eventData);
        }
    });

    public interface EventCallback {
        void accept(EventData server);
    }


    public record EventData(ServerLoginNetworkHandler serverLoginNetworkHandler, LoginKeyC2SPacket packet) {
    }
}
