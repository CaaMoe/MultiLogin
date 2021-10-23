package moe.caa.multilogin.core.impl;

/**
 * 传递报错的回调接口
 */
public interface CallbackTransmit<V> {

    /**
     * 当事件有结果时被调用
     *
     * @param value 事件结果
     */
    void solve(V value) throws Exception;
}
