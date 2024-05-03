package moe.caa.multilogin.core.resource.configuration.service

import moe.caa.multilogin.api.service.IService

abstract class BaseService(
    private val serviceId: Int,
    private val serviceName: String,
    val uuidGenerate: UUIDGenerateType,
    val whitelist: Boolean
): IService {
    override fun getServiceId() = serviceId
    override fun getServiceName() = serviceName

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
