package moe.caa.multilogin.velocity.command.sub

import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.command.parser.ServiceParser
import moe.caa.multilogin.velocity.config.service.BaseService
import org.incendo.cloud.parser.ParserDescriptor

class WhitelistCommand(
    private val handler: CommandHandler
) {
    fun register() {
        handler.register {
            literal("whitelist")
                .optional("service", ParserDescriptor.of(ServiceParser(), BaseService::class.java))

        }
    }
}