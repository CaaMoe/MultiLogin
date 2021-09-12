package moe.caa.multilogin.core.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 存放命令权限的类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Permissions {
    public static final String COMMAND_RELOAD = "multilogin.reload";
    public static final String COMMAND_QUERY_NAME = "multilogin.query.name";
    public static final String COMMAND_QUERY_REDIRECT_UUID = "multilogin.query.redirectuuid";
    public static final String COMMAND_QUERY_ONLINE_UUID = "multilogin.query.onlineuuid";
    public static final String COMMAND_QUERY_YGGDRASIL = "multilogin.query.yggdrasil";
    public static final String COMMAND_UPDATE = "multilogin.update";
    public static final String COMMAND_LIST = "multilogin.list";
    public static final String COMMAND_REMOVE = "multilogin.remove";
    public static final String COMMAND_CONFIRM = "multilogin.confirm";
    public static final String COMMAND_HELP = "multilogin.help";


    public static final String WHITELIST_ADD = "multilogin.whitelist.add";
    public static final String WHITELIST_REMOVE = "multilogin.whitelist.remove";
    public static final String WHITELIST_HAVE = "multilogin.whitelist.have";
}
