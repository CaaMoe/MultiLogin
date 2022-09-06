package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Contents {
    @Getter
    private static final Map<String, KickMessageEntry> kickMessageEntryMap = new ConcurrentHashMap<>();

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
