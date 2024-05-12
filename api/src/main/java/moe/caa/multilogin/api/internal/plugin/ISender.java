package moe.caa.multilogin.api.internal.plugin;

import org.jetbrains.annotations.ApiStatus;

/**
 * 公共命令执行者对象
 */
@ApiStatus.Internal
public interface ISender {

    /**
     * 这个命令执行者是不是一位玩家
     *
     * @return 是不是玩家
     */
    boolean isPlayer();

    /**
     * 判断是不是控制台
     */
    boolean isConsole();

    /**
     * 这个命令执行者是否具有某权限
     *
     * @return 是否具有某权限
     */
    boolean hasPermission(String permission);

    /**
     * 给执行者发送特定的字符串消息，使用精美的换行
     *
     * @param message 发送的字符串消息
     */
    /*
     * for (String s : message.split("\\r?\\n"))
     *     self.sendMessage(s);
     */
    void sendMessagePL(String message);

    /**
     * 获得这个命令执行者的名称
     *
     * @return 命令执行者的名称
     */
    String getName();

    /**
     * 获取对应的玩家对象
     *
     * @return 对应的玩家对象
     */
    IPlayer getAsPlayer();
}
