/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.main.MultiLoginCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.List;

public class MultiLoginCommand implements SimpleCommand {
    private final MultiCore core;

    public MultiLoginCommand(MultiCore core) {
        this.core = core;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        core.getCommandHandler().execute(new VelocitySender(source), invocation.alias(), args);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        return core.getCommandHandler().tabCompete(new VelocitySender(source), invocation.alias(), args);
    }
}
