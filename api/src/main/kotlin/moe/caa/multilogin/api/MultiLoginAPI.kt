package moe.caa.multilogin.api

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.api.service.BaseService
import java.util.*

class Player // 先放这, 后面改成 Velocity 的 ConnectedPlayer



interface MultiLoginAPI {

    /**
     * 向 MultiLogin 注册 Service
     *
     * @throws Exception 如果 Service 已注册或者 serviceId 重复
     */
    fun registerService(service: BaseService)


    /**
     * 通过给定的角色信息和 Service 实例查询它的绑定档案
     *
     * @throws Exception 如果 Service 没注册
     */
    fun findInGameProfile(service: BaseService, userProfile: GameProfile): GameProfile?

    /**
     * 通过给定的角色信息和 Service 实例查询它的绑定档案, 如果档案不存在则会自动生成档案
     *
     * @throws Exception 如果 Service 没注册
     */
    fun findOrCreateInGameProfile(service: BaseService, userProfile: GameProfile): GameProfile

    /**
     * 创建一份游戏内档案
     *
     * @throws Exception 如果 uuid 或 username 重复
     */
    fun createNewInGameProfile(uuid: UUID = UUID.randomUUID(), username: String): GameProfile

    /**
     * 绑定黑户玩家来源, 比如基岩版通过 Floodgate 加入游戏
     *
     * 注意, 如果是 Floodgate 需要在它的 API 中手动指定 link 为有效的档案, 否则不会过登录!!!
     *
     * 需要在 velocity 的 login 事件中绑定玩家来源, 否则将拒绝玩家登录游戏.
     *
     * @throws Exception 如果 当前玩家 已经绑定来源, 或者来源中所指定的信息错误
     */
    fun bindUnregisteredPlayerSource(player: Player, loginSource: LoginSource)
}