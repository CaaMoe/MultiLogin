package moe.caa.multilogin.api.main;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.command.CommandAPI;
import moe.caa.multilogin.api.handle.HandlerAPI;
import moe.caa.multilogin.api.language.LanguageAPI;
import moe.caa.multilogin.api.plugin.IPlugin;

public interface MultiCoreAPI {

    /**
     * 加载猫踢核心
     */
    void load() throws Exception;

    /**
     * 关闭猫踢核心
     */
    void close() throws Exception;

    /**
     * 返回命令处理程序
     */
    CommandAPI getCommandHandler();

    /**
     * 获得语言处理程序
     */
    LanguageAPI getLanguageHandler();

    /**
     * 返回混合验证处理程序
     */
    AuthAPI getAuthHandler();

    /**
     * 获得缓存
     */
    HandlerAPI getPlayerHandler();

    /**
     * 获得插件对象
     */
    IPlugin getPlugin();
}
