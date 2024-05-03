package moe.caa.multilogin.core.command.parser

import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.plugin.IPlayerManager
import moe.caa.multilogin.core.util.toUUIDOrNull
import org.incendo.cloud.caption.CaptionVariable
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class OnlinePlayerArgumentParser<C> : ArgumentParser<C, IPlayerManager.IPlayerInfo>, BlockingSuggestionProvider.Strings<C> {
    companion object {
        fun <C> playerParser(): ParserDescriptor<C, IPlayerManager.IPlayerInfo> =
            ParserDescriptor.of(OnlinePlayerArgumentParser(), IPlayerManager.IPlayerInfo::class.java)
    }

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<IPlayerManager.IPlayerInfo> {
        val playerName = commandInput.readString()
        val ifUuid = playerName.toUUIDOrNull()

        var player = MultiCore.instance.plugin.playerManager.getOnlinePlayer(playerName)
        if(player == null && ifUuid != null) player = MultiCore.instance.plugin.playerManager.getOnlinePlayer(ifUuid)

        if(player != null) return ArgumentParseResult.success(player)
        return ArgumentParseResult.failure(OnlinePlayerParseException(playerName, commandContext))
    }

    class OnlinePlayerParseException(private val username: String, context: CommandContext<*>) : ParserException(
        ServiceIdArgumentParser::class.java, context,
        StandardCaptionKeys.EXCEPTION_UNEXPECTED,
        CaptionVariable.of("input", username)
    )

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return MultiCore.instance.plugin.playerManager.getOnlinePlayers().map { it.inGameProfile.username }
    }
}