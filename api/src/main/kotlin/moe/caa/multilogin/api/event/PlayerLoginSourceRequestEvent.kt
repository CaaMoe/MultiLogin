package moe.caa.multilogin.api.event

import moe.caa.multilogin.api.data.LoginSource
import moe.caa.multilogin.api.exception.LoginSourceInGameProfileMismatchException
import moe.caa.multilogin.api.exception.LoginSourceRepeatSetException
import moe.caa.multilogin.api.player.MultiLoginPlayer

/**
 * 此事件在不同实现中的登录事件中触发, 用于记录玩家的有效来源.
 * 当事件结束后没有设置一个有效的来源的话将拒绝当前玩家登录游戏.
 *
 * 需要注意的是, 如果事件中的玩家的游戏档案未曾在 MultiLogin 记录过(名称以及uuid全部匹配), 在当前事件结束后将拒绝玩家登录游戏.
 * 所以在实现 Service 时, 需要在登录事件之前手动把玩家在游戏内的档案调整为已登记入库的档案,
 * 注意需要使用 UserManager 中的 findOrCreateInGameProfile 方法获取需要登录的游戏内档案, 你也可以不用他但是不推荐
 */
class PlayerLoginSourceRequestEvent(
    val player: MultiLoginPlayer
) {
    companion object {
        val eventHandler = createEventHandler<PlayerLoginSourceRequestEvent>()
    }


    private var source: LoginSource? = null

    /**
     * 设置玩家登录来源.
     *
     * @throws LoginSourceRepeatSetException 如果登录来源已被设置
     * @throws LoginSourceInGameProfileMismatchException 如果 source 中的 inGameProfile 与 player 的 profile 不匹配
     */
    fun setSource(source: LoginSource) = synchronized(this) {
        if (this.source != null) {
            throw LoginSourceRepeatSetException("duplicate set source.")
        }

        if (player.profile.minimalProfile != source.inGameProfile) {
            throw LoginSourceInGameProfileMismatchException("in game profile mismatch.")
        }

        this.source = source
    }

    fun getSource() = source
}