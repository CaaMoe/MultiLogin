package moe.caa.multilogin.core.resource.configuration.service

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
)