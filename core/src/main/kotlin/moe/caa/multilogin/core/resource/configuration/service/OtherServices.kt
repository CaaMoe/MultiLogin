package moe.caa.multilogin.core.resource.configuration.service

import moe.caa.multilogin.api.service.ServiceType

class FloodgateService(
    serviceId: Int,
    serviceName: String,
    uuidGenerate: UUIDGenerateType,
    whitelist: Boolean
) : BaseService(
    serviceId,
    serviceName,
    uuidGenerate,
    whitelist
) {
    override fun getServiceType() = ServiceType.FLOODGATE
}