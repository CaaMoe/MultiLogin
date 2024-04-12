package moe.caa.multilogin.core.resource

import moe.caa.multilogin.api.logger.logInfo
import moe.caa.multilogin.core.main.MultiCore
import java.io.File
import java.io.IOException
import java.io.InputStream


const val ROOT_CONFIGURATION = "configuration.conf"
const val MESSAGE_CONFIGURATION = "message.conf"

const val EXAMPLES_LITTLE_SKIN = "examples/littleskin.conf"
const val EXAMPLES_OFFICIAL = "examples/official.conf"
const val EXAMPLES_TEMPLATE_CN_FULL = "examples/template_cn_full.conf"
const val EXAMPLES_FLOODGATE = "examples/floodgate.conf"

fun saveDefaultResource(resource: String, cover: Boolean = false): File {
    val file = File(MultiCore.instance.plugin.dataFolder, resource)
    val exist = file.exists()

    if (cover || !exist) {
        file.parentFile?.mkdirs()

        getResource(resource).use { input -> file.outputStream().use { output -> input.transferTo(output) } }
        if (exist) {
            logInfo("Cover: $resource")
        } else {
            logInfo("Extract: $resource")
        }
    }

    return file;
}

fun getResource(resource: String): InputStream = MultiCore::class.java.getResourceAsStream("/$resource")
    ?: throw IOException("Failed processing resource ${resource}.")
