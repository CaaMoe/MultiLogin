package moe.caa.multilogin.api.internal.injector;

import moe.caa.multilogin.api.internal.main.MultiCoreAPI;

/**
 * 子模块注入接口
 */
public interface Injector {

    /**
     * 开始注入
     */
    void inject(MultiCoreAPI api) throws Throwable;
}
