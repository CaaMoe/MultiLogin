package moe.caa.multilogin.velocity.command.sub

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.command.*
import moe.caa.multilogin.velocity.command.parser.ProfileParser
import moe.caa.multilogin.velocity.command.parser.StringParser
import moe.caa.multilogin.velocity.util.*

class ProfileCommand(
    private val handler: CommandHandler
) {
    fun register() {
        handler.register {
            thenLiteral("profile") {
                permission(COMMAND_PROFILE_BASE)
                thenLiteral("create") {
                    permission(COMMAND_PROFILE_CREATE)
                    thenArgument("name", StringParser) {
                        thenArgumentOptional("uuid", TODO()) {
                            handler { handleCreate(this) }
                        }
                    }
                }
                thenLiteral("rename") {
                    permission(COMMAND_PROFILE_RENAME)
                    thenArgument("new_name", StringParser) {
                        thenArgument("profile", ProfileParser) {
                            handler { handleRename(this) }
                        }
                    }
                }
                thenLiteral("info") {
                    permission(COMMAND_PROFILE_INFO)
                    thenArgument("profile", ProfileParser) {
                        handler { handleInfo(this) }
                    }
                }
            }
        }
    }

    private fun handleInfo(context: CommandContext<CommandSource>) {
        TODO("Not yet implemented")
    }

    private fun handleRename(context: CommandContext<CommandSource>) {
        TODO("Not yet implemented")
    }

    private fun handleCreate(context: CommandContext<CommandSource>) {
        TODO("Not yet implemented")
    }
}