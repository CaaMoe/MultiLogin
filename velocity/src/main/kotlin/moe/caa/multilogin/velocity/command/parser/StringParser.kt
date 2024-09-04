package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType

object StringParser : ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        val argBeginning: Int = reader.cursor
        // 如果能读，并且下一个格子内容不是空
        while (reader.canRead() && reader.peek() != ' ') {
            // 游标++
            reader.skip()
        }
        return reader.string.substring(argBeginning, reader.cursor)
    }
}

