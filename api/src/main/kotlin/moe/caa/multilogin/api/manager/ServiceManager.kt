package moe.caa.multilogin.api.manager

import moe.caa.multilogin.api.exception.ServiceDuplicateRegistrationException
import moe.caa.multilogin.api.exception.ServiceIdDuplicateException
import moe.caa.multilogin.api.service.BaseService

interface ServiceManager {

    /**
     * 注册 Service
     *
     * @throws ServiceDuplicateRegistrationException 如果当前 Service 已被注册
     * @throws ServiceIdDuplicateException 如果 service id 重复
     */
    fun registerService(service: BaseService)

    /**
     * 取消注册一个 Service
     *
     * @return 是否取消成功(如果它本来就没有被注册时返回 false)
     */
    fun deregisterService(serviceId: Int): Boolean

    /**
     * 通过 service id 获取 BaseService 实例
     *
     * @return BaseService 实例
     */
    fun getServiceById(serviceId: Int): BaseService?

    /**
     * 获取已经注册的所有 Service 实例
     */
    fun getServices(): List<BaseService>
}