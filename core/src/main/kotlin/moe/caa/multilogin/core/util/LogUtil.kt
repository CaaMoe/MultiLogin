package moe.caa.multilogin.core.util

import moe.caa.multilogin.api.logger.LoggerProvider

fun logInfo(message: String? = null, throwable: Throwable? = null) = LoggerProvider.logger.info(message, throwable)
fun logWarn(message: String? = null, throwable: Throwable? = null) = LoggerProvider.logger.warn(message, throwable)
fun logError(message: String? = null, throwable: Throwable? = null) = LoggerProvider.logger.error(message, throwable)
fun logDebug(message: String? = null, throwable: Throwable? = null) = LoggerProvider.logger.debug(message, throwable)
