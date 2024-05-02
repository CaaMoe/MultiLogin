package moe.caa.multilogin.core.resource.configuration.service

import moe.caa.multilogin.api.service.IService
import moe.caa.multilogin.api.service.ServiceType

abstract class BaseService(
    val serviceId: Int,
    val serviceName: String,
    val uuidGenerate: UUIDGenerateType,
    val whitelist: Boolean
): IService {
    abstract val serviceType: ServiceType

    override fun getServiceId() = serviceId
    override fun getServiceName() = serviceName
    override fun getServiceType() = serviceType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BaseService
        return serviceId == other.serviceId
    }

    override fun hashCode(): Int {
        return serviceId
    }
}
