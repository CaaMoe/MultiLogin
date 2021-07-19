package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.command.commands.multilogin.MultiLoginCommand;
import moe.caa.multilogin.core.command.commands.whitelist.WhitelistCommand;
import moe.caa.multilogin.core.command.impl.RootCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    //    根命令
    private static final Map<String, RootCommand> fatherCommandMap = new HashMap<>();

    static {
//        记录根命令
        fatherCommandMap.put("whitelist", new WhitelistCommand());
        fatherCommandMap.put("multilogin", new MultiLoginCommand());
    }

    //    扫描根命令并执行
    public void execute(ISender sender, String command, String[] args) throws Throwable {
        if (!fatherCommandMap.containsKey(command = command.toLowerCase())) {
//        提示未知
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
            return;
        }
        RootCommand rootCommand = fatherCommandMap.get(command);
//                鉴权
        if (!rootCommand.canExecute(sender)) return;
//                执行
        rootCommand.execute(sender, args);
    }

    //    tab补全
    public List<String> tabCompete(ISender sender, String command, String[] args) {
        if (!fatherCommandMap.containsKey(command = command.toLowerCase())) {
//        提示未知
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
            return Collections.emptyList();
        }
        RootCommand rootCommand = fatherCommandMap.get(command);
        if (!rootCommand.canExecute(sender)) return Collections.emptyList();
        try {
            return rootCommand.tabComplete(sender, args);
        } catch (Throwable throwable) {
//            补全出错提示
            sender.sendMessage(LanguageKeys.COMPILE_ERROR.getMessage());
            MultiLogger.log(LoggerLevel.ERROR, throwable);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMPILE_ERROR.getMessage());
        }
//        无可补全
        return Collections.emptyList();
    }
}
