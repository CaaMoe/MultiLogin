package moe.caa.multilogin.core.handle;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓冲白名单处理工具
 */
public class CacheWhitelistHandler {
    @Getter
    private final Set<String> cachedWhitelist = Collections.newSetFromMap(new ConcurrentHashMap<>());
}
