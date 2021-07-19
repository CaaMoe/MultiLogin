/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.impl.FatherCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.impl;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.*;
import java.util.stream.Collectors;

public class FatherCommand extends Command {
    protected final Map<String, Command> subCommands = new HashMap<>();

    public FatherCommand(Permission permission) {
        super(permission);
    }

    //    向下一层执行(父节点默认
    protected final void executeSub(ISender sender, String[] args) throws Throwable {
        String name = args[0].toLowerCase();
        if (!subCommands.containsKey(name)) return;
        Command command = subCommands.get(name);
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        command.execute(sender, newArgs);

//        try {
//            execute(sender, args);
//        } catch (Throwable throwable) {
//            sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage());
//            MultiLogger.log(LoggerLevel.ERROR, throwable);
//            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
//        }
    }

    @Override
    public void execute(ISender sender, String[] args) throws Throwable {
        if (args.length < 1) return;
        executeSub(sender, args);
    }

    //    向下一层补全(父节点默认
    protected final List<String> tabCompleteSub(ISender sender, String[] args) throws Throwable {
        String name = args[0].toLowerCase();
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (subCommands.containsKey(name)) {
            return subCommands.get(name).tabComplete(sender, newArgs);
        }
        return tabComplete(sender, args).stream().filter(s -> ValueUtil.startsWithIgnoreCase(s, args[args.length - 1])).collect(Collectors.toList());
    }

    //    补全子命令(默认操作)
    public List<String> tabComplete(ISender sender, String[] args) throws Throwable {
        if (args.length < 1) return Collections.emptyList();
        if (args.length == 1) {
            ArrayList<String> canTab = new ArrayList<>();
            for (Map.Entry<String, Command> en : subCommands.entrySet()) {
                if (en.getValue().canExecute(sender)) canTab.add(en.getKey());
            }
            return canTab;
        } else {
            tabCompleteSub(sender, args);
        }
        return Collections.emptyList();
    }
}
