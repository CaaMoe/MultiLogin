package moe.caa.multilogin.api.service

import moe.caa.multilogin.api.LoginSource
import moe.caa.multilogin.api.profile.GameProfile

/**
 * 表示一个 Service
 *
 * Service 需要注册后才可以使用, 并且需要自己实现部分功能实现
 */
abstract class BaseService(
    val serviceId: Int
){

    /**
     * 白名单
     */
    var whitelist: Boolean = false

    /**
     * Service 的名字
     */
    abstract val serviceName: String

    /**
     * 通过当前角色信息生成一个新的游戏内档案.
     *
     * 当来自 Service 的新角色加入游戏即将分配游戏档案时调用, 此时应该返回一个最佳的和当前角色信息匹配的档案.
     *
     * 需要注意的是, 返回的内容如果 username 或 uuid 已被别的档案使用, 系统将会自动矫正.
     */
    abstract fun expectInGameProfile(userProfile: GameProfile): GameProfile
}