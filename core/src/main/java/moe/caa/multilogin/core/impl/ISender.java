/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.impl.ISender
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.impl;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * 可被core识别的命令执行者对象
 */
public interface ISender {

    /**
     * 获得名称
     *
     * @return 名称
     */
    String getSenderName();

    /**
     * 判断该命令执行者是否有某权限
     *
     * @param permission 某权限
     * @return 该命令执行者是否拥有某权限
     */
    boolean hasPermission(String permission);

    /**
     * 发送自定义消息给当前命令执行者
     *
     * @param text 消息对象
     */
    void sendMessage(BaseComponent text);

    /**
     * 判断该命令执行者是不是服务器操作员
     *
     * @return 该命令执行者是不是服务器操作员
     */
    boolean isOp();

    /**
     * 判断该命令执行者是不是一名游戏玩家
     *
     * @return 该命令执行者是不是一名游戏玩家
     */
    boolean isPlayer();
}
