package moe.caa.multilogin.api.manager

import moe.caa.multilogin.api.exception.InGameProfileNotFoundException
import moe.caa.multilogin.api.exception.ServiceNotRegisteredException
import moe.caa.multilogin.api.profile.MinimalProfile
import moe.caa.multilogin.api.service.BaseService
import java.util.*

interface UserManager {

    /**
     * 通过给定的角色信息和 Service 实例查询它的绑定档案
     *
     * @throws ServiceNotRegisteredException 如果 Service 未注册
     */
    fun findInGameProfile(service: BaseService, userProfile: MinimalProfile): MinimalProfile?

    /**
     * 通过给定的角色信息和 Service 实例查询它的绑定档案, 如果档案不存在则会根据提供的 userProfile 信息自动尝试生成档案
     *
     * @see moe.caa.multilogin.api.service.BaseService.expectInGameProfile
     * @throws ServiceNotRegisteredException 如果 Service 未注册
     */
    fun findOrCreateInGameProfile(service: BaseService, userProfile: MinimalProfile): MinimalProfile

    /**
     * 通过给定的登录方式设置它的 InGameProfile
     *
     * @throws InGameProfileNotFoundException 如果找不到 inGameProfile
     */
    fun setInGameProfile(service: BaseService, userProfile: MinimalProfile, inGameProfileUUID: UUID)
}