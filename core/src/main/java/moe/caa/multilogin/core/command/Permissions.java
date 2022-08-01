package moe.caa.multilogin.core.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 储存权限的地方
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissions {
    public static final String COMMAND_TAB_COMPLETE = "command.multilogin.tab.complete";
    public static final String COMMAND_MULTI_LOGIN_RELOAD = "command.multilogin.reload";
    public static final String COMMAND_MULTI_LOGIN_ERASE_USERNAME = "command.multilogin.eraseusername";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_ADD = "command.multilogin.whitelist.add";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_REMOVE = "command.multilogin.whitelist.remove";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_ADD = "command.multilogin.whitelist.permanent.add";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_REMOVE = "command.multilogin.whitelist.permanent.remove";
    public static final String COMMAND_MULTI_LOGIN_LIST = "command.multilogin.list";
    public static final String COMMAND_MULTI_LOGIN_QUERY_LOGIN_BYNAME = "command.multilogin.query.login.byname";
    public static final String COMMAND_MULTI_LOGIN_QUERY_LOGIN_BYINGAMEUUID = "command.multilogin.query.login.byingameuuid";
    public static final String COMMAND_MULTI_LOGIN_QUERY_IN_GAME_UUID_BYNAME = "command.multilogin.query.ingameuuid.byname";
    public static final String COMMAND_MULTI_LOGIN_QUERY_IN_GAME_UUID_BYPROFILE = "command.multilogin.query.ingameuuid.byprofile";
}