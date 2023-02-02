package moe.caa.multilogin.core.handle;

import lombok.Getter;
import moe.caa.multilogin.api.util.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 临时档案切换处理工具
 */
public class TemplateProfileRedirectHandler {

    @Getter
    private final Map<Pair<Integer, UUID>, UUID> templateProfileRedirectMap = new ConcurrentHashMap<>();
}
