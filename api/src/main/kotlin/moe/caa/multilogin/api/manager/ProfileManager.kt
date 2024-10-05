package moe.caa.multilogin.api.manager

import moe.caa.multilogin.api.exception.UUIDDuplicationException
import moe.caa.multilogin.api.exception.UsernameDuplicationException
import moe.caa.multilogin.api.profile.MinimalProfile
import java.util.*

interface ProfileManager {

    /**
     * 通过给定的档案信息和 Service 实例查询它的绑定档案
     */
    fun findInGameProfile(username: String): MinimalProfile?

    /**
     * 通过给定的档案信息和 Service 实例查询它的绑定档案
     */
    fun findInGameProfile(profileId: UUID): MinimalProfile?

    /**
     * 通过给定的档案信息创建一个游戏内档案出来
     *
     * @throws UUIDDuplicationException 如果uuid重复了
     * @throws UsernameDuplicationException 如果username重复了
     */
    fun createInGameProfile(id: UUID = UUID.randomUUID(), username: String): MinimalProfile

    /**
     * 重命名游戏内档案
     *
     * @throws UsernameDuplicationException 如果username重复了
     */
    fun renameInGameProfile(id: UUID, newUsername: String): MinimalProfile

    /**
     * 重命名游戏内档案
     *
     * @throws UsernameDuplicationException 如果username重复了
     */
    fun renameInGameProfile(profile: MinimalProfile, newName: String) = renameInGameProfile(profile.id, newName)
}