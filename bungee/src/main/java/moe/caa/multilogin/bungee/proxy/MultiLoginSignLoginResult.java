/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.proxy;

import net.md_5.bungee.connection.LoginResult;

public class MultiLoginSignLoginResult extends LoginResult {
    //    继承BungeeCord登入结果 公开构造方法
    public MultiLoginSignLoginResult(LoginResult result) {
        super(result.getId(), result.getName(), result.getProperties());
    }
}