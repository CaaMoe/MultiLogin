package moe.caa.multilogin.fabric.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;

/**
 * 登录阶段玩家被动断开链接事件
 */
public class LoginStatePlayerDisconnectEvent {
    public static final Event<EventCallback> INSTANCE = EventFactory.createArrayBacked(EventCallback.class, callbacks -> eventData -> {
        for (EventCallback callback : callbacks) {
            callback.accept(eventData);
        }
    });

    public interface EventCallback {
        void accept(EventData server);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class EventData {
        private final ServerLoginNetworkHandler serverLoginNetworkHandler;
        private final Text disconnectText;
    }
}
