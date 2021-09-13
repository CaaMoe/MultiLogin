package moe.caa.multilogin.core.impl;

/**
 * 异步回调接口
 */
public interface Callback<V> {

    /**
     * 当事件有结果时被调用
     *
     * @param value     事件结果
     * @param throwable 事件错误，如果有的话
     */
    void solve(V value, Throwable throwable);
}
