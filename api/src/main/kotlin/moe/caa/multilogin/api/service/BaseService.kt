package moe.caa.multilogin.api.service

import moe.caa.multilogin.api.profile.MinimalProfile

/**
 * 表示一个 Service, 所有的 Service 都需要注册才能使用.
 *
 * 要实现完整的功能, 还需要实现一下对 PlayerLoginSourceRequestEvent 的监听, 然后在你的目标玩家登录时设置它的登录来源.
 *
 * @see moe.caa.multilogin.api.event.PlayerLoginSourceRequestEvent
 */
abstract class BaseService(
    val serviceId: Int,
    val serviceName: String
) {

    /**
     * 通过当前角色信息生成一个新的游戏内档案.
     *
     * 当来自 Service 的新角色加入游戏即将分配游戏档案或 api 主动调用时调用, 此时应该返回一个最佳的和当前角色信息匹配的档案.
     *
     * 需要注意的是, 返回的内容如果 username 或 uuid 已被别的档案使用, 系统将会自动矫正.
     *
     * @see moe.caa.multilogin.api.manager.UserManager.findOrCreateInGameProfile
     */
    open fun expectInGameProfile(userProfile: MinimalProfile): MinimalProfile = userProfile
}