package moe.caa.multilogin.velocity.auth.yggdrasil

import kotlinx.coroutines.*
import moe.caa.multilogin.velocity.config.service.yggdrasil.BaseYggdrasilService
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import org.apache.logging.log4j.core.util.Integers

class YggdrasilAuthenticationHandler(
    val plugin: MultiLoginVelocity
) : IYggdrasilService{

    override suspend fun authenticate(
        username: String,
        serverId: String,
        playerIp: String
    ): YggdrasilAuthenticationResult {
        val service = plugin.config.serviceMap
            .values
            .filterIsInstance<BaseYggdrasilService>()

        if(service.isEmpty()){
            return YggdrasilAuthenticationResult.Failure(YggdrasilAuthenticationResult.Failure.Reason.NO_YGGDRASIL_SERVICES)
        }

        // 请求优先
        // 从 UserData 表中查这个 onlineName 对应的可能的 service id
        // 然后把他们转成对应的 baseYggdrasilService 对象
        val preferredList = plugin.database.useDatabase {
            UserDataTableV3.select(
                UserDataTableV3.serviceId
            ).where {
                UserDataTableV3.onlineName eq username
            }.map {
                it[UserDataTableV3.serviceId]
            }.map { id ->
                service.first {
                    it.baseServiceSetting.serviceId == id
                }
            }
        }

        // 第二队列
        val secondaryList = service.filter { !preferredList.contains(it) }

        // 然后开始验证
        TODO()
    }

    private fun auth(
        baseYggdrasilServices: List<BaseYggdrasilService>,
        username: String,
        serverId: String,
        playerIp: String
    ) = runBlocking {
            val completableDeferred = CompletableDeferred<YggdrasilAuthenticationResult>()

            val failures = ArrayList<YggdrasilAuthenticationResult.Failure>()
            var size = baseYggdrasilServices.size
            for (service in baseYggdrasilServices) {
                launch {
                    try {
                        when (val result = service.authenticate(username, serverId, playerIp)) {
                            is YggdrasilAuthenticationResult.Failure -> failures.add(result)
                            is YggdrasilAuthenticationResult.Success -> completableDeferred.complete(result)
                        }
                    } finally {
                        --size
                        if(size == 0){
                            val failedResult = failures.map { it.reason }.maxByOrNull { it.ordinal } ?: YggdrasilAuthenticationResult.Failure.Reason.NO_YGGDRASIL_SERVICES
                            completableDeferred.complete(YggdrasilAuthenticationResult.Failure(failedResult))
                        }
                    }
                }
            }
        return@runBlocking completableDeferred.await()
    }
}
