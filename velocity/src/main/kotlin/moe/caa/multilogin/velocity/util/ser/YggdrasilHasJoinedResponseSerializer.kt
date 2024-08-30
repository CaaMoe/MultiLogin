package moe.caa.multilogin.velocity.util.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilHasJoinedResponse
import moe.caa.multilogin.velocity.util.toUUIDOrNull

object YggdrasilHasJoinedResponseSerializer : KSerializer<YggdrasilHasJoinedResponse> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "YggdrasilHasJoinedResponse", PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): YggdrasilHasJoinedResponse {
        val element = decoder.decodeSerializableValue(JsonElement.serializer())

        if (element !is JsonObject) {
            throw SerializationException("Invalid JsonElement.")
        }

        val jsonObject = element.jsonObject
        val id = jsonObject["id"]?.jsonPrimitive?.content?.toUUIDOrNull()
            ?: throw SerializationException("Profile id is required.")
        val name = jsonObject["name"]?.jsonPrimitive?.content
        val properties = ArrayList<GameProfile.Property>()

        jsonObject["properties"].apply {
            var jsonObjects: List<JsonObject>? = null

            when (this) {
                is JsonArray -> jsonObjects = this.filterIsInstance<JsonObject>()
                is JsonObject -> jsonObjects = this.map { e -> e.value }.filterIsInstance<JsonObject>()
                else -> {}
            }

            jsonObjects?.forEach {
                properties.add(
                    GameProfile.Property(
                        it["name"]?.jsonPrimitive?.content
                            ?: throw SerializationException("Property name is required."),
                        it["value"]?.jsonPrimitive?.content
                            ?: throw SerializationException("Property value is required."),
                        it["signature"]?.jsonPrimitive?.content
                    )
                )
            }
        }

        val profileActions =
            jsonObject["profileActions"]?.jsonArray?.filterIsInstance<JsonPrimitive>()?.map { it.content } ?: listOf()

        return YggdrasilHasJoinedResponse(id, name, properties, profileActions)
    }

    override fun serialize(encoder: Encoder, value: YggdrasilHasJoinedResponse) {
        buildJsonObject {
            put("id", value.uuid.toString().replace("-", ""))
            put("name", value.username)
            putJsonArray("properties") {
                value.properties.forEach {
                    add(buildJsonObject {
                        put("name", it.name)
                        put("value", it.value)
                        put("signature", it.signature)
                    })
                }
            }
            putJsonArray("profileActions") {
                value.actions.forEach {
                    add(JsonPrimitive(it))
                }
            }
        }.apply {
            encoder.encodeSerializableValue(JsonElement.serializer(), this)
        }
    }
}