package moe.caa.multilogin.core.command.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;
import moe.caa.multilogin.core.command.CommandManager;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * 命令处理程序抽象类
 */
@AllArgsConstructor
@Getter
public abstract class AbstractHandler {
    private final CommandManager commandManager;
    private final String name;

    /**
     * 执行某条指令
     *
     * @param sender 命令执行者
     * @param args   命令参数
     * @return 是否有匹配的执行
     */
    public abstract boolean execute(ISender sender, String[] args) throws Exception;

    /**
     * 判断一个命令执行者是不是一个玩家，否则向该执行者发送不是玩家的命令反馈
     *
     * @param sender 命令执行者
     * @return 是不是一位玩家
     */
    public boolean isPlayerOrSendFeedback(ISender sender) {
        var ret = sender.isPlayer();
        if (!ret)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_is_player", FormatContent.empty()));
        return ret;
    }

    /**
     * 判断一个命令执行者是否拥有某权限，否则向该执行者发送没有命令权限的美丽的反馈
     *
     * @param sender 命令执行者
     * @return 有没有权限
     */
    public boolean hasPermission(ISender sender, String permission) {
        var ret = sender.hasPermission(permission);
        if (!ret)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_no_permission", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("permission").content(permission).build()
            )));
        return ret;
    }

    /**
     * 判断一个字符串是不是一个合法的 UUID，否则向该执行者发送不是合法的 UUID 的命令反馈
     *
     * @param sender 命令执行者
     * @param str    uuid字符串
     * @return UUID 实例
     */
    public UUID getUuidOrSendFeedback(ISender sender, String str) {
        var uuid = ValueUtil.getUuidOrNull(str);
        if (uuid == null)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_is_uuid", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("uuid_string").content(str).build()
            )));
        return uuid;
    }

    /**
     * 尝试通过 onlineUuid 获取一个玩家数据，否则向该执行者发送找不到玩家数据的命令反馈
     *
     * @param sender 命令执行者
     * @param s      在线UUID
     * @return 玩家数据档案实例
     */
    public User getUserByOnlineUuidOrSendFeedback(ISender sender, String s) throws SQLException {
        var uuid = getUuidOrSendFeedback(sender, s);
        if (uuid == null) return null;
        return getUserByOnlineUuidOrSendFeedback(sender, uuid);
    }

    /**
     * 尝试通过 onlineUuid 获取一个玩家数据，否则向该执行者发送找不到玩家数据的命令反馈
     *
     * @param sender 命令执行者
     * @param uuid   在线UUID
     * @return 玩家数据档案实例
     */
    public User getUserByOnlineUuidOrSendFeedback(ISender sender, UUID uuid) throws SQLException {
        var user = commandManager.getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(uuid);
        if (user == null)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_found_user_data_by_online_uuid", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("uuid").content(uuid).build()
            )));
        return user;
    }

    /**
     * 尝试通过 redirectUuid 获取多个玩家数据，否则向该执行者发送找不到玩家数据的命令反馈
     *
     * @param sender 命令执行者
     * @param s      重定向的 UUID
     * @return 玩家数据档案实例
     */
    public List<User> getUserByRedirectUuidOrSendFeedback(ISender sender, String s) throws SQLException {
        var uuid = getUuidOrSendFeedback(sender, s);
        if (uuid == null) return null;
        return getUserByRedirectUuidOrSendFeedback(sender, uuid);
    }

    /**
     * 尝试通过 redirectUuid 获取多个玩家数据，否则向该执行者发送找不到玩家数据的命令反馈
     *
     * @param sender 命令执行者
     * @param uuid   重定向的 UUID
     * @return 玩家数据档案实例
     */
    public List<User> getUserByRedirectUuidOrSendFeedback(ISender sender, UUID uuid) throws SQLException {
        var user = commandManager.getCore().getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(uuid);
        if (user == null)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_found_user_data_by_redirect_uuid", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("uuid").content(uuid).build()
            )));
        return user;
    }

    /**
     * 尝试通过 name 获取多个玩家数据，若为空则向该执行者发送找不到玩家数据的命令反馈
     *
     * @param sender 命令执行者
     * @param name   当前用户名
     * @return 玩家数据档案实例
     */
    public List<User> getUserByCurrentNameOrSendFeedback(ISender sender, String name) throws SQLException {
        var users = commandManager.getCore().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(name);
        if (users.isEmpty())
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_found_user_data_by_current_name", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name").content(name).build()
            )));
        return users;
    }

    /**
     * 尝试通过 path 获取一个 Yggdrasil 账户验证服务器实例，否则向该执行者发送找不到 Yggdrasil 账户验证服务器的命令反馈
     *
     * @param sender 命令执行者
     * @param path   Yggdrasil 账户验证服务器配置路径
     * @return Yggdrasil 账户验证服务器的配置实例
     */
    public YggdrasilService getYggdrasilServiceOrSendFeedback(ISender sender, String path) {
        var ret = commandManager.getCore().getYggdrasilServicesHandler().getYggdrasilService(path);
        if (ret == null)
            sender.sendMessage(commandManager.getCore().getLanguageHandler().getMessage("command_not_found_yggdrasil", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name").content(name).build()
            )));
        return ret;
    }
}
