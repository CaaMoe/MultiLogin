package moe.caa.multilogin.velocity.config.service.yggdrasil

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.caa.multilogin.velocity.auth.yggdrasil.IYggdrasilService
import moe.caa.multilogin.velocity.auth.yggdrasil.LoginProfile
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.ser.YggdrasilHasJoinedResponseSerializer
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
            delayMillis { yggdrasilServiceSetting.delayRetry.toLong() }
            retryIf(maxRetries = yggdrasilServiceSetting.retry) { _, response -> !response.status.isSuccess() }
            retryOnException(maxRetries = yggdrasilServiceSetting.retry, retryOnTimeout = true)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = yggdrasilServiceSetting.timeout.toLong()
        }

        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    plugin.logDebug(message)
                }
            }
        }
    }

    override suspend fun authenticate(
        loginProfile: LoginProfile
    ): YggdrasilAuthenticationResult {
        try {
            val requestParam = LinkedHashMap<String, String>()
            customYggdrasilServiceSetting.requestParametersTemplate.forEach { (k, v) ->
                requestParam[k] = v
                    .replace("{username}", loginProfile.username)
                    .replace("{serverId}", loginProfile.serverId)
                    .replace("{playerIp}", if (yggdrasilServiceSetting.preventProxy) loginProfile.playerIp else "")
            }
            requestParam.values.removeIf { it.isEmpty() }

            val requestBuilder = HttpRequestBuilder().apply {
                url(String(customYggdrasilServiceSetting.hasJoinedUrl))

                when (customYggdrasilServiceSetting.hasJoinedRequestMode) {
                    HasJoinedRequestMode.POST -> {
                        method = HttpMethod.Post
                        setBody(TextContent(Json.encodeToString(requestParam), ContentType.Application.Json))
                    }

                    HasJoinedRequestMode.GET -> {
                        method = HttpMethod.Get
                        requestParam.forEach { (k, v) ->
                            parameter(k, v)
                        }
                    }
                }
            }

            val response = httpClient.request(requestBuilder)
            if (response.status == HttpStatusCode.NoContent) {
                return YggdrasilAuthenticationResult.Failure(YggdrasilAuthenticationResult.Failure.Reason.INVALID_SESSION)
            }

            if (response.status != HttpStatusCode.OK) {
                return YggdrasilAuthenticationResult.Failure(YggdrasilAuthenticationResult.Failure.Reason.SERVER_BREAK_DOWN)
            }

            val authenticationResult =
                Json.decodeFromString(YggdrasilHasJoinedResponseSerializer, response.bodyAsText())
            return YggdrasilAuthenticationResult.Success(this, authenticationResult.toGameProfile(loginProfile))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            plugin.logDebug(
                "An exception occurred while validating the session, service: ${
                    baseServiceSetting.serviceId
                }, loginProfile: $loginProfile.", e
            )
            return YggdrasilAuthenticationResult.Failure(YggdrasilAuthenticationResult.Failure.Reason.SERVER_BREAK_DOWN)
        }
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
        val delayRetry: Int
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