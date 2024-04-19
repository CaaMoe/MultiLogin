package moe.caa.multilogin.core.plugin

import net.kyori.adventure.text.Component

interface ICommandSender {
    fun sendMessage(text: Component)
    fun sendMessage(text: () -> Component) = sendMessage(text.invoke())
}