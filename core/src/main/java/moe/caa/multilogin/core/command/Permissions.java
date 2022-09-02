package moe.caa.multilogin.core.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 储存权限的地方
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissions {
    public static final String COMMAND_TAB_COMPLETE = "command.multilogin.tab.complete";
    public static final String COMMAND_MULTI_LOGIN_ME = "command.multilogin.me";
    public static final String COMMAND_MULTI_LOGIN_RELOAD = "command.multilogin.reload";
    public static final String COMMAND_MULTI_LOGIN_CONFIRM = "command.multilogin.confirm";
    public static final String COMMAND_MULTI_LOGIN_ERASE_USERNAME = "command.multilogin.eraseusername";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_ADD = "command.multilogin.whitelist.add";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_REMOVE = "command.multilogin.whitelist.remove";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_ADD = "command.multilogin.whitelist.permanent.add";
    public static final String COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_REMOVE = "command.multilogin.whitelist.permanent.remove";
    public static final String COMMAND_MULTI_LOGIN_LIST = "command.multilogin.list";
    public static final String COMMAND_MULTI_LOGIN_SEARCH_LOGIN_BYNAME = "command.multilogin.search.login.byname";
    public static final String COMMAND_MULTI_LOGIN_SEARCH_LOGIN_BYINGAMEUUID = "command.multilogin.search.login.byingameuuid";
    public static final String COMMAND_MULTI_LOGIN_SEARCH_IN_GAME_UUID_BYNAME = "command.multilogin.search.ingameuuid.byname";
    public static final String COMMAND_MULTI_LOGIN_SEARCH_IN_GAME_UUID_BYPROFILE = "command.multilogin.search.ingameuuid.byprofile";
    public static final String COMMAND_MULTI_LOGIN_SEARCH_CURRENT = "command.multilogin.search.current";
    public static final String COMMAND_MULTI_LOGIN_USER_MERGE_BYNAME = "command.multilogin.user.merge.byname";
    public static final String COMMAND_MULTI_LOGIN_USER_MERGE_BYINGAMEUUID = "command.multilogin.user.merge.byingameuuid";
    public static final String COMMAND_MULTI_LOGIN_USER_MERGEME_BYNAME = "command.multilogin.user.mergeme.byname";
    public static final String COMMAND_MULTI_LOGIN_USER_MERGEME_BYINGAMEUUID = "command.multilogin.user.mergeme.byingameuuid";
    public static final String COMMAND_MULTI_LOGIN_USER_REMOVE_PROFILE = "command.multilogin.user.remove.profile";
}