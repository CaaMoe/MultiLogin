package moe.caa.multilogin.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerQuitEvent {
    public static final Event<EventCallback> INSTANCE = EventFactory.createArrayBacked(EventCallback.class, callbacks -> eventData -> {
        for (EventCallback callback : callbacks) {
            callback.accept(eventData);
        }
    });

    public interface EventCallback {
        void accept(EventData server);
    }


    public record EventData(ServerPlayerEntity serverPlayer) {
    }
}
