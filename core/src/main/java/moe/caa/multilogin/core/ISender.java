package moe.caa.multilogin.core;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * 可被core识别的命令执行者对象
 */
public interface ISender {

    /**
     * 获得名称
     * @return 名称
     */
    String getSenderName();

    /**
     * 判断该命令执行者是否有某权限
     * @param permission 某权限
     * @return 该命令执行者是否拥有某权限
     */
    boolean hasPermission(String permission);

    /**
     * 发送自定义消息给当前命令执行者
     * @param text 消息对象
     */
    void sendMessage(BaseComponent text);

    /**
     * 判断该命令执行者是不是服务器操作员
     * @return 该命令执行者是不是服务器操作员
     */
    boolean isOp();

    /**
     * 判断该命令执行者是不是一名游戏玩家
     * @return 该命令执行者是不是一名游戏玩家
     */
    boolean isPlayer();
}
