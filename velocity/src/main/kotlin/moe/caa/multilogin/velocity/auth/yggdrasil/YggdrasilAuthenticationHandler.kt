package moe.caa.multilogin.velocity.auth.yggdrasil

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Failure
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Success
import moe.caa.multilogin.velocity.config.service.yggdrasil.BaseYggdrasilService
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.util.concurrent.CopyOnWriteArrayList

class YggdrasilAuthenticationHandler(
    val plugin: MultiLoginVelocity
) {

    suspend fun auth(
        loginProfile: LoginProfile
    ): YggdrasilAuthenticationResult {
        val service = plugin.config.serviceMap.values.filterIsInstance<BaseYggdrasilService>()

        if(service.isEmpty()){
            return Failure(Failure.Reason.NO_YGGDRASIL_SERVICES)
        }

        // 请求优先
        // 从 UserData 表中查这个 onlineName 对应的可能的 service id
        // 然后把他们转成对应的 baseYggdrasilService 对象
        val preferredList = plugin.database.useDatabase {
            UserDataTableV3.select(
                UserDataTableV3.serviceId
            ).where {
                UserDataTableV3.onlineName eq loginProfile.username
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

        val result = auth(listOf(preferredList, secondaryList), loginProfile)
        if (result is Success) {
            plugin.logDebug(
                "${result.profile.username}(uuid: ${
                    result.profile.uuid
                }) from yggdrasil service ${
                    result.baseYggdrasilService.baseServiceSetting.serviceName
                }(service id: ${
                    result.baseYggdrasilService.baseServiceSetting.serviceId
                }) has been authenticated, pass."
            )
        }

        return result
    }

    private suspend fun auth(
        services: List<List<BaseYggdrasilService>>,
        loginProfile: LoginProfile,
    ): YggdrasilAuthenticationResult {
        return coroutineScope {
            return@coroutineScope services.map { service ->
                when (val result = auth(service, loginProfile)) {
                    is Success -> return@coroutineScope result
                    is Failure -> return@map result
                }
                // 返回一个最坏的结果
            }.mostFailures()
        }
    }

    @JvmName("auth0")
    private suspend fun auth(
        services: List<BaseYggdrasilService>,
        loginProfile: LoginProfile
    ): YggdrasilAuthenticationResult {
        if (services.isEmpty()) return Failure(Failure.Reason.NO_YGGDRASIL_SERVICES)

        plugin.logDebug(
            "Verifying the $loginProfile session, current yggdrasil services: [${
                services.map { it.baseServiceSetting.serviceId }.joinToString(", ")
            }]."
        )

        return coroutineScope {
            val completableDeferred = CompletableDeferred<YggdrasilAuthenticationResult>()
            val failures = ArrayList<Failure>()
            var size = services.size

            val mutex = Mutex()
            for (service in services) {
                launch(Dispatchers.IO) {
                    val result = service.authenticate(loginProfile)
                    mutex.withLock {
                        try {
                            when (result) {
                                is Failure -> failures.add(result)
                                is Success -> completableDeferred.complete(result)
                            }
                        } finally {
                            --size
                            if (size == 0 && !completableDeferred.isCompleted) {
                                completableDeferred.complete(failures.mostFailures())
                            }
                        }
                    }
                }
            }
            // 确保一个成功返回, 就马上返回
            // 否则等待所有任务, 拿到最失败的任务返回
            val result = completableDeferred.await()
            return@coroutineScope result
        }
    }

    // 返回最坏的结果
    private fun Collection<Failure>.mostFailures() = this.maxByOrNull { it.reason.ordinal }?: Failure(Failure.Reason.NO_YGGDRASIL_SERVICES)
}

