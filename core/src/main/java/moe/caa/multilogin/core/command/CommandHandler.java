/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.CommandHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import moe.caa.multilogin.core.command.commands.WhitelistCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CommandHandler {

    private static final CommandDispatcher<ISender> DISPATCHER = new CommandDispatcher<>();

    static {
        WhitelistCommand.register(DISPATCHER);
    }

    public void execute(ISender sender, String command, String[] args) {
        ParseResults<ISender> parse = DISPATCHER.parse(command + " " + String.join(" ", args), sender);
        try {
            DISPATCHER.execute(parse);
        } catch (CommandSyntaxException e) {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }

    //    tab补全
    public List<String> tabCompete(ISender sender, String command, String[] args) {
        ParseResults<ISender> parse = DISPATCHER.parse(command + " " + String.join(" ", args), sender);
        CompletableFuture<Suggestions> suggestions = DISPATCHER.getCompletionSuggestions(parse);
        try {
            return suggestions.get().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ignored) {
        }
        return null;
    }

    public static LiteralArgumentBuilder<ISender> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ISender, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
