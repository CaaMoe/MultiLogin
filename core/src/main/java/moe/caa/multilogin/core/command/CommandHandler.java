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

import moe.caa.multilogin.core.command.commands.MultiLoginCommand;
import moe.caa.multilogin.core.command.commands.WhitelistCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.*;

/**
 * 命令解析器程序
 */
public class CommandHandler {

    public final Map<String, SubCommand> rootCommand = new Hashtable<>();

    private final MultiCore core;

    public CommandHandler(MultiCore core) {
        this.core = core;
        rootCommand.put("whitelist", new WhitelistCommand(core).registerSub());
        rootCommand.put("multilogin", new MultiLoginCommand(core).registerSub());
    }

    /**
     * 提交执行一个命令
     *
     * @param sender  命令执行者
     * @param command 命令根名称
     * @param args    命令参数
     */
    public void execute(ISender sender, String command, String[] args) {
        SubCommand sub = rootCommand.get(command.toLowerCase(Locale.ROOT));
        if (sub != null) {
            if (sub.hasPermission(sender)) {
                if (sub.execute(sender, args)) {
                    return;
                }
            }
        }
        // 不知道命令
        sender.sendMessage("未知的命令");
    }

    /**
     * 获得命令补全建议
     *
     * @param sender  命令执行者
     * @param command 命令根名称
     * @param args    命令参数
     * @return 建议
     */
    public List<String> tabCompete(ISender sender, String command, String[] args) {
        SubCommand sub = rootCommand.get(command.toLowerCase(Locale.ROOT));
        if (sub == null) return Collections.emptyList();

        // 判断有没有对应的权限补全参数
        if (sub.hasPermission(sender)) {
            return sub.tabCompete(sender, args);
        }
        return Collections.emptyList();
    }
}
