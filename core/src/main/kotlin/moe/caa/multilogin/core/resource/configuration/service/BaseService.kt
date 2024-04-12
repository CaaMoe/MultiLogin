package moe.caa.multilogin.core.resource.configuration.service

abstract class BaseService(
    val serviceId: Int,
    val serviceName: String,
    val uuidGenerate: UUIDGenerateType,
    val whitelist: Boolean
) {

    abstract val serviceType: ServiceType;

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
