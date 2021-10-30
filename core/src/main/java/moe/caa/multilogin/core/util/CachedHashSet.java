package moe.caa.multilogin.core.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 擦车集合
 */
public class CachedHashSet<VALUE> {
    private final long retainTime;
    private final ConcurrentHashMap<VALUE, Long> contents = new ConcurrentHashMap<>();

    /**
     * 构建这个集合
     *
     * @param retainTime 保留时常（毫秒）
     */
    public CachedHashSet(long retainTime) {
        this.retainTime = retainTime;
    }

    /**
     * 添加元素到集合中
     *
     * @param value 元素值
     */
    public void add(VALUE value) {
        contents.put(value, System.currentTimeMillis());
    }

    /**
     * 移除元素
     *
     * @param value 元素值
     */
    public void remove(VALUE value) {
        contents.remove(value);
    }

    /**
     * 获得当前元素集合
     *
     * @return 当前元素集合
     */
    public Set<VALUE> getEntrySet() {
        clearInValid();
        return contents.keySet();
    }

    /**
     * 清理辣鸡元素
     */
    public void clearInValid() {
        contents.entrySet().removeIf(entry -> entry.getValue() + retainTime < System.currentTimeMillis());
    }
}
