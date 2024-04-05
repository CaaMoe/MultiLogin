package moe.caa.multilogin.core.resource.builddata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

@Serializable
data class BuildData(
    @SerialName("version") val version: String,
    @SerialName("build_by") val buildBy: String,
    @SerialName("contributors") val contributors: String,
    @SerialName("build_type") val buildType: String,
    @SerialName("build_revision") val buildRevision: String,
    @SerialName("build_timestamp") val buildTimestamp: String,
) {
    fun forceDebugMode() = buildType.equals("final", true).not()

}

val buildData: BuildData = Json.decodeFromString(
    BuildData::class.java.getResourceAsStream("/builddata.json").use { input ->
        InputStreamReader(input!!, Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }
)