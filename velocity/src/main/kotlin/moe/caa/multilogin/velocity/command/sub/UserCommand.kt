package moe.caa.multilogin.velocity.command.sub

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.command.COMMAND_USER_BASE
import moe.caa.multilogin.velocity.command.COMMAND_USER_INFO
import moe.caa.multilogin.velocity.command.COMMAND_USER_SET_PROFILE
import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.command.parser.ProfileParser
import moe.caa.multilogin.velocity.command.parser.UserParser
import moe.caa.multilogin.velocity.util.handler
import moe.caa.multilogin.velocity.util.permission
import moe.caa.multilogin.velocity.util.thenArgument
import moe.caa.multilogin.velocity.util.thenLiteral

class UserCommand(
    private val handler: CommandHandler
) {
    fun register() {
        handler.register {
            thenLiteral("user") {
                permission(COMMAND_USER_BASE)
                thenLiteral("info") {
                    permission(COMMAND_USER_INFO)
                    thenArgument("user", UserParser) {
                        handler { handleUser(this) }
                    }
                }
                thenLiteral("setProfile") {
                    permission(COMMAND_USER_SET_PROFILE)
                    thenArgument("profile", ProfileParser) {
                        thenArgument("user", UserParser) {
                            handler { handleSetProfile(this) }
                        }
                    }
                }
            }
        }
    }

    private fun handleSetProfile(context: CommandContext<CommandSource>) {
        TODO("Not yet implemented")
    }

    private fun handleUser(context: CommandContext<CommandSource>) {
        TODO("Not yet implemented")
    }
}