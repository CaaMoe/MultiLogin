package moe.caa.multilogin.core.resource.builddata

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.caa.multilogin.core.main.MultiCore
import java.io.InputStreamReader

private val buildDataElementObject = Json.parseToJsonElement(
    MultiCore::class.java.getResourceAsStream("/builddata").use { input ->
        InputStreamReader(input!!, Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }).jsonObject

fun getBuildData(key: String) = buildDataElementObject[key]?.jsonPrimitive?.content ?: "Unknown"
val showWarning = buildDataElementObject["build_type"]?.jsonPrimitive?.content?.equals("final", true) != true