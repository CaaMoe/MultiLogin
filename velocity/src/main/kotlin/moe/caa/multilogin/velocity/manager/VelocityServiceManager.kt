package moe.caa.multilogin.velocity.manager

import moe.caa.multilogin.api.exception.ServiceDuplicateRegistrationException
import moe.caa.multilogin.api.exception.ServiceIdDuplicateException
import moe.caa.multilogin.api.manager.ServiceManager
import moe.caa.multilogin.api.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class VelocityServiceManager(
    private val plugin: MultiLoginVelocity
): ServiceManager {
    private val lock = ReentrantReadWriteLock()
    private val serviceIdMap = mutableMapOf<Int, BaseService>()

    override fun registerService(service: BaseService) = lock.write {
        if (serviceIdMap.values.contains(service)) {
            throw ServiceDuplicateRegistrationException("Duplicate registration.")
        }
        if(serviceIdMap.keys.contains(service.serviceId)){
            throw ServiceIdDuplicateException("service id ${service.serviceId} duplication.")
        }

        plugin.logger.info("Add a authentication service with id ${service.serviceId} and name ${service.serviceName}.")
        serviceIdMap[service.serviceId] = service
    }

    override fun deregisterService(serviceId: Int) = lock.write {
        val service = serviceIdMap.remove(serviceId)
        if(service != null){
            plugin.logger.info("Remove the authentication service with id ${service.serviceId} and name ${service.serviceName}.")
            return@write true
        }
        return@write false
    }

    override fun getServiceById(serviceId: Int): BaseService? = lock.read {
        serviceIdMap[serviceId]
    }

    override fun getServices(): List<BaseService> = lock.read {
        return serviceIdMap.values.toList()
    }
}