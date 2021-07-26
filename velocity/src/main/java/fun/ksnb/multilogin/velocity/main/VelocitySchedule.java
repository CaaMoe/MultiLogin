/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.main.VelocitySchedule
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.main;

import moe.caa.multilogin.core.impl.AbstractScheduler;

public class VelocitySchedule extends AbstractScheduler {
    //    Velocity也不需要同步
    @Override
    public void runTask(Runnable run, long delay) {
        runTaskAsync(run, delay);
    }

    @Override
    public void runTask(Runnable run) {
        runTaskAsync(run);
    }
}
