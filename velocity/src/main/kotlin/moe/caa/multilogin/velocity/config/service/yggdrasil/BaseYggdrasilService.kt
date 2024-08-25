package moe.caa.multilogin.velocity.config.service.yggdrasil

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import moe.caa.multilogin.velocity.auth.yggdrasil.IYggdrasilService
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.io.File

abstract class BaseYggdrasilService(
    plugin: MultiLoginVelocity,
    resourceFile: File,
    baseServiceSetting: BaseServiceSetting,
    private val yggdrasilServiceSetting: YggdrasilServiceSetting,
    private val customYggdrasilServiceSetting: CustomYggdrasilServiceSetting
) : BaseService(
    plugin, resourceFile, baseServiceSetting
), IYggdrasilService {
    private val httpClient = HttpClient(CIO) {
        install(UserAgent) {
            agent = "MultiLogin/3.0"
        }

        install(HttpRequestRetry) {
            maxRetries = yggdrasilServiceSetting.retry
            delayMillis { yggdrasilServiceSetting.delayRetry.toLong() }
            retryIf { _, response -> !response.status.isSuccess() }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = yggdrasilServiceSetting.timeout.toLong()
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    plugin.logDebug(message)
                }
            }
        }
    }

    override suspend fun authenticate(
        username: String,
        serverId: String,
        playerIp: String
    ): YggdrasilAuthenticationResult {
        val requestParam = HashMap<String, String>()
        customYggdrasilServiceSetting.requestParametersTemplate.forEach { (k, v) ->
            requestParam[k] = v
                .replace("{username}", username)
                .replace("{serverId}", serverId)
                .replace("{playerIp}", if (yggdrasilServiceSetting.preventProxy) playerIp else "")
        }
        requestParam.values.removeIf { it.isEmpty() }

        val requestBuilder = HttpRequestBuilder().apply {
            url(String(customYggdrasilServiceSetting.hasJoinedUrl))
            retry {

            }
        }

        val response = when (customYggdrasilServiceSetting.hasJoinedRequestMode) {
            HasJoinedRequestMode.POST -> httpClient.post(requestBuilder)
            HasJoinedRequestMode.GET -> httpClient.get(requestBuilder)
        }

        TODO()
    }


    enum class HasJoinedRequestMode {
        POST,
        GET
    }

    data class YggdrasilServiceSetting(
        // 阻止代理
        val preventProxy: Boolean,
        // 超时
        val timeout: Int,
        // 重试
        val retry: Int,
        // 重试等待
        val delayRetry: Int,

        )

    data class CustomYggdrasilServiceSetting(
        // 请求方式
        val hasJoinedRequestMode: HasJoinedRequestMode,
        // 请求链接
        val hasJoinedUrl: ByteArray,
        // 请求参数
        val requestParametersTemplate: Map<String, String>
    )
}