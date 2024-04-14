package moe.caa.multilogin.core.auth.service.yggdrasil

import moe.caa.multilogin.api.auth.LoginProfile
import moe.caa.multilogin.api.logger.logDebug
import moe.caa.multilogin.api.logger.logWarn
import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilService
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class YggdrasilAuthenticator(
    private val authenticationHandler: AuthenticationHandler
) {

    fun auth(loginProfile: LoginProfile): YggdrasilAuthenticationResult {
        val services = GeneralConfiguration.services.map { it.value }.filterIsInstance<YggdrasilService>()
        if (services.isEmpty()) return YggdrasilAuthenticationFailureResult.generate(
            YggdrasilAuthenticationFailureReason.NO_YGGDRASIL_SERVICE
        )

        val primariesAuth = ArrayList<YggdrasilService>()
        if (services.size == 1) {
            primariesAuth.add(services[0])
        } else {
            // todo 这里通过 username 从数据库中拿 service ids
        }

        val secondariesAuth = services.filter { !primariesAuth.contains(it) }

        return when (val primariesAuthResult = hasJoined("primaries", loginProfile, primariesAuth)) {
            is YggdrasilAuthenticationSuccessResult -> primariesAuthResult
            is YggdrasilAuthenticationFailureResult ->
                when (val secondariesAuthResult = hasJoined("secondaries", loginProfile, secondariesAuth)) {
                    is YggdrasilAuthenticationSuccessResult -> secondariesAuthResult
                    is YggdrasilAuthenticationFailureResult ->
                        listOf(primariesAuthResult, secondariesAuthResult).maxBy { it.failedReason.severity }
                }
        }
    }

    private fun hasJoined(
        flag: String,
        loginProfile: LoginProfile,
        authList: Collection<YggdrasilService>
    ): YggdrasilAuthenticationResult {
        if (authList.isEmpty()) return YggdrasilAuthenticationFailureResult.generate(
            YggdrasilAuthenticationFailureReason.NO_YGGDRASIL_SERVICE
        )

        logDebug(
            "Starting Yggdrasil session authentication for ${loginProfile.username}: ($flag)[${
                authList.map { it.serviceId }.joinToString()
            }]"
        )


        val countDownLatch = CountDownLatch(1)
        val tasks = Collections.synchronizedList(ArrayList(authList))

        val succeedAuthenticationResult = AtomicReference<YggdrasilAuthenticationSuccessResult>()
        val failedAuthenticationResults =
            Collections.synchronizedList(ArrayList<YggdrasilAuthenticationFailureResult>())

        authList.forEach { service ->
            authenticationHandler.multiCore.asyncExecute.submit {
                try {
                    when (val authenticationResult = YggdrasilAuthenticationTask(service, loginProfile).hasJoined()) {
                        is YggdrasilAuthenticationSuccessResult -> {
                            if (!succeedAuthenticationResult.compareAndSet(null, authenticationResult)) {
                                logWarn("############################################################")
                                logWarn("#                          WARNING                          ")
                                logWarn("#   THE DETECTION OF MULTIPLE VALID YGGDRASIL SESSIONS FOR ")
                                logWarn("# A PLAYER MAY LEAD TO CONFUSION AND POTENTIAL LOSS IN THE ")
                                logWarn("# GAME PROFILE!!! ")
                                logWarn("#")
                                logWarn("# CURRENT PLAYER: ${loginProfile.username}")
                                logWarn(
                                    "# CURRENT YGGDRASIL SERVICES: ${authenticationResult.service.serviceId}(${authenticationResult.service.serviceName}), ${succeedAuthenticationResult.get().service.serviceId}(${succeedAuthenticationResult.get().service.serviceName})"
                                )
                                logWarn("#")
                                logWarn("#   PLEASE VERIFY THE PRESENCE OF DUPLICATE YGGDRASIL ")
                                logWarn("# SERVICE CONFIGURATIONS!!!")
                                logWarn("############################################################")
                            } else {
                                logDebug("The yggdrasil session of ${loginProfile.username} has been authenticated by service ${authenticationResult.service.serviceId}.")
                            }
                            countDownLatch.countDown()
                        }

                        is YggdrasilAuthenticationFailureResult -> failedAuthenticationResults.add(authenticationResult)
                    }
                } finally {
                    tasks.remove(service)
                    if (tasks.isEmpty()) countDownLatch.countDown()
                }
            }
        }
        countDownLatch.await()

        return succeedAuthenticationResult.get() ?: failedAuthenticationResults.maxBy { it.failedReason.severity }
    }
}