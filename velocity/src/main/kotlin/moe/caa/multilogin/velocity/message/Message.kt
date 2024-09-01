package moe.caa.multilogin.velocity.message

import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.getResource
import moe.caa.multilogin.velocity.util.saveDefaultResource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.loader.HeaderMode
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 消息处理工具
 */
class Message(
    private val plugin: MultiLoginVelocity
) {
    private var messageResource: ConfigurationNode = HoconConfigurationLoader.builder().buildAndLoadString("")

    /**
     * 重载消息文件
     */
    fun init() = reload()
    fun reload() {
        // 默认的消息文件
        val defaultMessageResource = HoconConfigurationLoader.builder().source {
            BufferedReader(InputStreamReader(getResource("message.conf"), Charsets.UTF_8))
        }.build().load()

        // 读取外置的文件, 补全缺失的消息节点
        val file = saveDefaultResource(plugin.dataDirectory.toFile(), "message.conf")
        messageResource = HoconConfigurationLoader.builder().file(file).build().load().apply {
            var messing = false
            defaultMessageResource.childrenMap().entries.forEach {
                if (!hasChild(it.key)) {
                    messing = true
                    node(it.key).set(it.value)

                    plugin.logger.warn("Missing message node: ${it.key}")
                }
            }

            if (messing) {
                HoconConfigurationLoader.builder().headerMode(HeaderMode.PRESERVE).file(file).build().save(this)

                plugin.logger.info("All missing message nodes have been replaced.")
            }
        }
    }

    /**
     * 通过给定的键, 读出对应的 Component
     */
    fun message(node: String): Component {
        try {
            val configurationNode = messageResource.node(node.split("."))

            return MiniMessage.miniMessage().deserialize(
                if (configurationNode.isList) {
                    configurationNode.getList(String::class.java)?.joinToString { "\n" } ?: ""
                } else {
                    configurationNode.getString("")
                }
            )
        } catch (e: Throwable) {
            plugin.logger.error("Error occurred with minimessage deserialization(node: $node)", e)
            return Component.text("Message Error: $node").color(TextColor.color(1.0F, 0F, 0F))
        }
    }
}


// 内容替换函数
fun Component.replace(literal: String, replacement: String) =
    replaceText(
        TextReplacementConfig.builder()
            .matchLiteral(literal)
            .replacement(replacement)
            .build()
    )

fun Component.replace(literal: String, replacement: Any) = this.replace(literal, replacement.toString())
fun Component.replace(literal: String, replacement: Component) =
    replaceText(
        TextReplacementConfig.builder()
            .matchLiteral(literal)
            .replacement(replacement)
            .build()
    )