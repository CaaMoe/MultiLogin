package moe.caa.multilogin.core.auth.service.yggdrasil.serialize

import kotlinx.serialization.json.*
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.auth.service.yggdrasil.LoginProfile
import moe.caa.multilogin.core.util.toUUIDOrNull

object GameProfileDeserializer {
    fun deserialize(loginProfile: LoginProfile, element: JsonElement): GameProfile? {
        if (element !is JsonObject) return null

        val id = element["id"]?.jsonPrimitive?.content?.toUUIDOrNull() ?: return null
        val name = element["name"]?.jsonPrimitive?.content ?: loginProfile.username

        val properties = when (val propertiesElement = element["properties"]) {
            is JsonArray -> propertiesElement.map { PropertyDeserializer.deserialize(it) }
            is JsonObject -> propertiesElement.map { PropertyDeserializer.deserialize(it.value) }
            is JsonNull, null -> listOf()
            else -> throw ParseException("Unknown properties type: ${propertiesElement.javaClass.name}")
        }

        return GameProfile(id, name, properties)
    }

    object PropertyDeserializer {
        fun deserialize(element: JsonElement) = GameProfile.Property(
            element.jsonObject["name"]?.jsonPrimitive?.content ?: throw ParseException("property name is null."),
            element.jsonObject["value"]?.jsonPrimitive?.content ?: throw ParseException("property value is null."),
            element.jsonObject["signature"]?.jsonPrimitive?.content
        )
    }

    class ParseException(message: String) : RuntimeException(message)
}