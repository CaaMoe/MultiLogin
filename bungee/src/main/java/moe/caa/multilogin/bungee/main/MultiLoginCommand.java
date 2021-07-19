/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.main.MultiLoginCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.main;

import moe.caa.multilogin.core.command.CommandHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MultiLoginCommand extends Command implements TabExecutor {
    private static CommandHandler commandHandler = new CommandHandler();

    public MultiLoginCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            commandHandler.execute(new BungeeSender(sender), getName(), args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return commandHandler.tabCompete(new BungeeSender(sender), getName(), args);
    }
}
