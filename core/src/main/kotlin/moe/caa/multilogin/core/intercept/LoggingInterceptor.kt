package moe.caa.multilogin.core.intercept

import moe.caa.multilogin.api.logger.logDebug
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.TimeUnit

/**
 * Http 日志打印拦截器
 */
object LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBody: RequestBody? = request.body

        logDebug("--> ${request.method} ${request.url}")
        if (requestBody != null) {
            val bf = Buffer()
            requestBody.writeTo(bf)
            val contentLength = bf.size
            if (contentLength > 0) {
                logDebug("--> ($contentLength bytes)")
                requestBody.toString()
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logDebug("<-- HTTP FAILED", e)
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        logDebug("<-- ${response.code} ${response.request.url} (${tookMs}ms)")
        val body = response.body
        if (body != null) {
            body.source().request(Long.MAX_VALUE)
            val contentLength = body.source().buffer.size
            if (contentLength > 0) logDebug("<-- ($contentLength bytes)")
        }

        return response
    }
}