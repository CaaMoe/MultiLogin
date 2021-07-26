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

import moe.caa.multilogin.core.command.commands.multilogin.MultiLoginCommand;
import moe.caa.multilogin.core.command.commands.whitelist.WhitelistCommand;
import moe.caa.multilogin.core.command.impl.RootCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;

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
    public void execute(ISender sender, String command, String[] args) {
        if (!fatherCommandMap.containsKey(command = command.toLowerCase())) {
//        提示未知
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
            return;
        }
        RootCommand rootCommand = fatherCommandMap.get(command);
//                鉴权
        if (!rootCommand.canExecute(sender)) {
//            sender.sendMessage(LanguageKeys.COMMAND_NO_PERMISSION.getMessage());
            return;
        }
//                执行
        try {
            rootCommand.execute(sender, args);
        } catch (Throwable throwable) {
//            补全出错提示
            sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage());
            MultiCore.getInstance().getLogger().log(LoggerLevel.ERROR, throwable);
            MultiCore.getInstance().getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
        }
    }

    //    tab补全
    public List<String> tabCompete(ISender sender, String command, String[] args) {
        if (!fatherCommandMap.containsKey(command = command.toLowerCase())) {
//        提示未知
//          sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
            return Collections.emptyList();
        }
        RootCommand rootCommand = fatherCommandMap.get(command);
        if (!rootCommand.canExecute(sender)) return Collections.emptyList();
        try {
            return rootCommand.tabComplete(sender, args);
        } catch (Throwable throwable) {
//            补全出错提示
//            sender.sendMessage(LanguageKeys.COMPILE_ERROR.getMessage());
            MultiCore.getInstance().getLogger().log(LoggerLevel.ERROR, throwable);
//            MultiCore.getInstance().getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMPILE_ERROR.getMessage());
        }
//        无可补全
        return Collections.emptyList();
    }
}
