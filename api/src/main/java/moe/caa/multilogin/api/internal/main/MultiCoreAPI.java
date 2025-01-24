package moe.caa.multilogin.api.internal.main;

import moe.caa.multilogin.api.MapperConfigAPI;
import moe.caa.multilogin.api.internal.auth.AuthAPI;
import moe.caa.multilogin.api.internal.command.CommandAPI;
import moe.caa.multilogin.api.internal.handle.HandlerAPI;
import moe.caa.multilogin.api.internal.language.LanguageAPI;
import moe.caa.multilogin.api.internal.plugin.IPlugin;
import moe.caa.multilogin.api.internal.skinrestorer.SkinRestorerAPI;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
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
     * 返回皮肤修复程序
     */
    SkinRestorerAPI getSkinRestorerHandler();

    /**
     * 获得缓存
     */
    HandlerAPI getPlayerHandler();

    /**
     * 获得版本映射
     */
    MapperConfigAPI getMapperConfig();

    /**
     * 获得插件对象
     */
    IPlugin getPlugin();
}
