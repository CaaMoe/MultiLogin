package moe.caa.multilogin.core.resource.configuration.service

import moe.caa.multilogin.core.resource.configuration.ReadConfigurationException

abstract class BaseService(
    val serviceId: Int,
    val serviceName: String,
    val uuidGenerate: UUIDGenerateType,
    val whitelist: Boolean
) {
    init {
        if (serviceId > 127 || serviceId < 0) throw ReadConfigurationException("Service id $serviceId is out of bounds, The value can only be between 0 and 127.")
    }

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
