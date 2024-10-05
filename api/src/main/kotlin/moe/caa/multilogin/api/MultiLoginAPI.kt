package moe.caa.multilogin.api

import moe.caa.multilogin.api.manager.OnlineDataProvider
import moe.caa.multilogin.api.manager.ProfileManager
import moe.caa.multilogin.api.manager.ServiceManager
import moe.caa.multilogin.api.manager.UserManager

interface MultiLoginAPI {
    /**
     * 档案管理 API
     */
    val profileManager: ProfileManager

    /**
     * service管理 API
     */
    val serviceManager: ServiceManager

    /**
     * user管理 API
     */
    val userManager: UserManager

    /**
     * 角色信息提供者
     */
    val onlineDataProvider: OnlineDataProvider
}