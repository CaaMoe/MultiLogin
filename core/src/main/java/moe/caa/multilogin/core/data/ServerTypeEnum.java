/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.ServerTypeEnum
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data;

public enum ServerTypeEnum {

    /**
     * 表示Blessing skin验证类型服务器
     */
    BLESSING_SKIN,

    /**
     * 表示 Minecraft 原版验证类型服务器
     */
    MINECRAFT,

    /**
     * 其他类型验证服务器
     */
    CUSTOM;
}