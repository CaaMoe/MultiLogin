package moe.caa.multilogin.api.user;

import moe.caa.multilogin.api.OperationException;

import java.util.Set;
import java.util.UUID;

public interface UserManager {

    /**
     * 通过用户在线 UUID 检索用户实例<br>
     * 在当前版本中，返回的 SET 集合只可能为空或长度为1
     * @param onlineUuid 用户在线 UUID
     * @return 用户实例
     */
    Set<User> getUserFromOnlineUuid(UUID onlineUuid);

    /**
     * 通过游戏内 UUID 检索用户实例
     * @param redirectUuid 游戏内 UUID
     * @return 用户实例
     */
    Set<User> getUsersFromRedirectUuid(UUID redirectUuid);

    /**
     * 通过游戏内用户名检索用户实例
     * @param name 游戏内用户名
     * @return 用户实例
     */
    Set<User> getUsersFromName(String name, boolean nameIgnoreCase);

    /**
     * 检索所有用户实例
     * @return 用户实例
     */
    Set<User> getAllUser();

    /**
     * 创建新的用户实例
     * @param onlineUuid 用户在线 UUID
     * @return 新的用户实例
     */
    User createNewUser(UUID onlineUuid);

    /**
     * 写入新的用户数据
     * @param newUser 新的用户数据
     */
    void insertNewUser(User newUser) throws OperationException;

    /**
     * 提交用户数据修改
     * @param user 修改后的用户数据
     */
    void updateUser(User user) throws OperationException;
}
