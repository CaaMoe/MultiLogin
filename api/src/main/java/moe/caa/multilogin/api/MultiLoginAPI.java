package moe.caa.multilogin.api;

import moe.caa.multilogin.api.skinrestorer.SkinRestorerManager;
import moe.caa.multilogin.api.user.UserManager;
import moe.caa.multilogin.api.yggdrasil.YggdrasilManager;

/**
 * MultiLogin Api
 */
public interface MultiLoginAPI {

    /**
     * 返回用户管理程序
     * @return 用户管理程序
     */
    UserManager getUserManager();

    /**
     * 返回 Yggdrasil 管理程序
     * @return Yggdrasil 管理程序
     */
    YggdrasilManager getYggdrasilManager();

    /**
     * 返回 SkinRestorer 管理程序
     * @return SkinRestorer 管理程序
     */
    SkinRestorerManager getSkinRestorerManager();
}
