package moe.caa.multilogin.api.manager

import moe.caa.multilogin.api.exception.InGameProfileNotFoundException
import moe.caa.multilogin.api.exception.ProfileConflictException
import moe.caa.multilogin.api.profile.MinimalProfile
import java.util.*

interface ProfileManager {

    /**
     * 通过给定的档案名称查询档案
     */
    fun findInGameProfile(username: String): MinimalProfile?

    /**
     * 通过给定的档案UUID查询档案
     */
    fun findInGameProfile(profileId: UUID): MinimalProfile?

    /**
     * 通过给定的档案信息创建一个游戏内档案出来
     *
     * @throws ProfileConflictException 如果档案冲突
     */
    fun createInGameProfile(id: UUID = UUID.randomUUID(), username: String): MinimalProfile

    /**
     * 重命名游戏内档案
     *
     * @throws ProfileConflictException 如果档案冲突
     * @throws InGameProfileNotFoundException 如果档案不存在
     */
    fun renameInGameProfile(id: UUID, newUsername: String)
}