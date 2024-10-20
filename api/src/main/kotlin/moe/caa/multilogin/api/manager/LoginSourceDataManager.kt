package moe.caa.multilogin.api.manager

import moe.caa.multilogin.api.data.LoginSource
import moe.caa.multilogin.api.player.MultiLoginPlayer
import java.util.*

interface LoginSourceDataManager {
    /**
     * 通过游戏内UUID获取当前在线档案持有用户登录信息
     */
    fun findLoginSourceByInGameUUID(inGameUUID: UUID): LoginSource?
}