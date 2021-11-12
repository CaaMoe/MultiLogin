package moe.caa.multilogin.api.user;

import java.util.UUID;

/**
 * 用户对象
 */
public interface User {

    /**
     * 返回用户在线 UUID<br>
     * 此 UUID 为账户身份验证服务器中传入的值
     *
     * @return 用户在线 UUID
     */
    UUID getOnlineUuid();

    /**
     * 返回用户在数据库中记录的用户名
     * @return 用户名称
     */
    String getName();

    /**
     * 设置用户在数据库中缓存的用户名<br>
     * 此操作并不能应用于下次用户登入时所显示的用户名
     * @param newName 新的用户名
     */
    void setName(String newName);

    /**
     * 返回用户在服务器内的 UUID<br>
     * 此 UUID 为通过在线 UUID 及指定规律生成的值
     *
     * @return 用户在服务器内的 UUID
     */
    UUID getRedirectUuid();

    /**
     * 设置用户在游戏内的 UUID<br>
     * 注意，此操作将会丢失用户数据
     *
     * @param newRedirectUuid 新的游戏内 UUID
     */
    void setRedirectUuid(UUID newRedirectUuid);

    /**
     * 返回指定验证该名用户的身份验证服务器路径
     * @return 指定验证该名用户的身份验证服务器路径
     */
    String getAuthYggdrasilPath();

    /**
     * 设置指定验证该名用户的身份验证服务器路径<br>
     * 注意，此操作将会影响玩家登入方式
     *
     * @param newAuthYggdrasilPath 新的账户身份验证服务器路径
     */
    void setAuthYggdrasilPath(String newAuthYggdrasilPath);

    /**
     * 返回该名用户是否具有白名单
     *
     * @return 用户是否具有白名单
     */
    boolean hasWhitelist();

    /**
     * 设置用户是否具有白名单
     * @param newWhitelist 新的用户是否具有白名单
     */
    void setWhitelist(boolean newWhitelist);
}
