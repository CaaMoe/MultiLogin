package moe.caa.multilogin.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

/**
 * 插件启动事件
 */
public class PluginEnableEvent {

    // 插件启动事件
    public static final Event<eventCallback> INSTANCE = EventFactory.createArrayBacked(eventCallback.class, callbacks -> server -> {
        for (eventCallback callback : callbacks) {
            callback.enable(server);
        }
    });

    public interface eventCallback {
        void enable(MinecraftDedicatedServer server);
    }
}
