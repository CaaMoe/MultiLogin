package moe.caa.multilogin.core.auth.service.yggdrasil

import kotlinx.serialization.json.Json
import moe.caa.multilogin.core.auth.service.yggdrasil.serialize.GameProfileDeserializer
import moe.caa.multilogin.core.intercept.LoggingInterceptor
import moe.caa.multilogin.core.intercept.RetryInterceptor
import moe.caa.multilogin.core.resource.configuration.service.HttpMethodType
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilService
import moe.caa.multilogin.core.util.logError
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

class YggdrasilAuthenticationTask(
    private val yggdrasilService: YggdrasilService,
    private val loginProfile: LoginProfile
) {
    fun hasJoined(): YggdrasilAuthenticationResult {
        try {
            val request = when (yggdrasilService.httpMethodType) {
                HttpMethodType.GET -> Request.Builder()
                    .url(yggdrasilService.generateAuthUrl(loginProfile))
                    .header("User-Agent", "MultiLogin/3.0")
                    .build()

                HttpMethodType.POST -> TODO("POST")
            }

            val timeout = Duration.ofMillis(yggdrasilService.timeout.toLong())
            OkHttpClient.Builder()
                .addInterceptor(RetryInterceptor(yggdrasilService.retry, yggdrasilService.delayRetry))
                .addInterceptor(LoggingInterceptor)
                .callTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .connectTimeout(timeout)
                .build().newCall(request).execute().use { response ->

                    val gameProfile = response.body?.string()?.let {
                        if (it.isEmpty()) {
                            null
                        } else {
                            GameProfileDeserializer.deserialize(loginProfile, Json.parseToJsonElement(it))
                        }
                    }

                    if (gameProfile != null) {
                        return YggdrasilAuthenticationSuccessResult(yggdrasilService, gameProfile)
                    } else {
                        return YggdrasilAuthenticationFailureResult.generate(YggdrasilAuthenticationFailureReason.INVALID_SESSION)
                    }
                }
        } catch (e: Throwable) {
            logError(
                "An exception occurred during authentication of the yggdrasil service whose ID is ${yggdrasilService.serviceId}.",
                e
            )
            return YggdrasilAuthenticationFailureResult.generate(YggdrasilAuthenticationFailureReason.SERVER_BREAK_DOWN)
        }
    }
}