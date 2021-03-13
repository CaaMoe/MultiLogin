/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.util.I18n
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.util;

import moe.caa.multilogin.core.MultiCore;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle rb;

    public static void initService() {
        rb = ResourceBundle.getBundle("lang/multilogin");
        MultiCore.info(getTransString("load_language"));
    }

    public static String getTransString(String key, Object... args) {
        return MessageFormat.format(rb.getString(key), args);
    }
}
