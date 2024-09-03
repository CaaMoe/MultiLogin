package moe.caa.multilogin.velocity.auth.yggdrasil

import kotlinx.coroutines.*
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Failure
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Success
import moe.caa.multilogin.velocity.config.service.yggdrasil.BaseYggdrasilService
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import kotlin.coroutines.coroutineContext

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
        return services.map { service ->
            when (val result = auth(service, loginProfile)) {
                is Success -> return result
                is Failure -> return@map result
            }
            // 返回一个最坏的结果
        }.mostFailures()

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

        val requestJob = Job(coroutineContext[Job])
        val requestScope = CoroutineScope(requestJob)


        // 结果
        val result = CompletableDeferred<YggdrasilAuthenticationResult>()
        // 所有请求队列
        val requests = services.map { service ->
            requestScope.async {
                when (val sub = service.authenticate(loginProfile)) {
                    is Failure -> return@async sub
                    // 只要请求成功了, 马上完成这个任务
                    is Success -> result.complete(sub)
                }
                return@async null
            }
        }

        // 全部请求都没有好的结果
        requestScope.launch {
            // 返回一个最坏的结果
            requests.mapNotNull { it.await() }.mostFailures().apply {
                if (!result.isCompleted) {
                    result.complete(this)
                }
            }
        }
        return result.await()
    }

    // 返回最坏的结果
    private fun Collection<Failure>.mostFailures() = this.maxByOrNull { it.reason.ordinal }?: Failure(Failure.Reason.NO_YGGDRASIL_SERVICES)
}

