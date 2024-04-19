package moe.caa.multilogin.core.intercept

import moe.caa.multilogin.core.util.logDebug
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    private val maxRetries: Int,
    private val retryDelay: Int
) : Interceptor {
    private var retries = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        while (true) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                if (maxRetries <= retries) throw e
                retries++
                logDebug("Request failed, retrying ($retries/$maxRetries)...", e)
                if (retryDelay > 0) Thread.sleep(retryDelay.toLong())
            }
        }
    }
}