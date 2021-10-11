package moe.caa.multilogin.core.loader.impl;

/**
 * 公共的插件引导程序
 */
public abstract class BasePluginBootstrap {

    /**
     * 插件加载时调用
     */
    public abstract void onLoad();

    /**
     * 插件启用时调用
     */
    public abstract void onEnable();

    /**
     * 插件卸载时调用
     */
    public abstract void onDisable();
}
