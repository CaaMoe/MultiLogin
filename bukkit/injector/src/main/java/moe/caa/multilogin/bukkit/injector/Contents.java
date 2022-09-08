package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 常烂
 */
public class Contents {

    // 记录ID对应的踢出信息
    @Getter
    private static final Map<Object, KickMessageEntry> kickMessageEntryMap = new ConcurrentHashMap<>();

    // 封装的踢出信息
    // 记录了收录时间，和踢出的信息
    @Getter
    @AllArgsConstructor
    public static class KickMessageEntry {
        private final long signTime;
        private final String kickMessage;

        public static KickMessageEntry of(String kickMessage) {
            return new KickMessageEntry(System.currentTimeMillis(), kickMessage);
        }
    }
}
