package moe.caa.multilogin.core.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 储存权限的地方
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissions {
    public static final String COMMAND_MULTI_LOGIN_RELOAD = "command.multilogin.reload";
    public static final String COMMAND_MULTI_LOGIN_UPDATE = "command.multilogin.update";
    public static final String COMMAND_MULTI_LOGIN_CONFIRM = "command.multilogin.confirm";
    public static final String COMMAND_MULTI_LOGIN_LIST = "command.multilogin.list";
    public static final String COMMAND_MULTI_LOGIN_SKIN_RESTORER_REMOVE = "command.multilogin.skinrestorer.remove";
    public static final String COMMAND_MULTI_LOGIN_SKIN_RESTORER_REMOVE_ALL = "command.multilogin.skinrestorer.remove.all";
    public static final String COMMAND_MULTI_LOGIN_SKIN_USERDATA_REMOVE = "command.multilogin.userdata.remove";
    public static final String COMMAND_MULTI_LOGIN_SKIN_USERDATA_MODIFY_YGGDRASIL = "command.multilogin.userdata.modify.yggdrasil";
    public static final String COMMAND_MULTI_LOGIN_SKIN_USERDATA_MODIFY_REDIRECT_UUID = "command.multilogin.userdata.modify.redirectUuid";
    public static final String COMMAND_MULTI_LOGIN_SKIN_USERDATA_INFO = "command.multilogin.userdata.info";
    public static final String COMMAND_WHITELIST_ADD = "command.multilogin.whitelist.add";
    public static final String COMMAND_WHITELIST_REMOVE = "command.multilogin.whitelist.remove";
    public static final String COMMAND_WHITELIST_LIST = "command.multilogin.whitelist.list";
    public static final String COMMAND_WHITELIST_CLEAR_CACHE = "command.multilogin.whitelist.clearCache";
}
