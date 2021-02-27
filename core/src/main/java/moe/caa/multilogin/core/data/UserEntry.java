package moe.caa.multilogin.core.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 表示数据库中存放的玩家对象
 */
public class UserEntry {

    private final String online_uuid;
    private String current_name;
    private String redirect_uuid;
    private String yggdrasil_service;
    private int whitelist;
    private transient YggdrasilServiceEntry serviceEntry;

    public UserEntry(String online_uuid, String current_name, String redirect_uuid, String yggdrasil_service, int whitelist) {
        this.online_uuid = online_uuid;
        this.current_name = current_name;
        this.redirect_uuid = redirect_uuid;
        this.yggdrasil_service = yggdrasil_service;
        this.whitelist = whitelist;
        this.serviceEntry = PluginData.getYggdrasilServerEntry(yggdrasil_service);
    }

    /**
     * 获得在线UUID
     * @return 在线UUID
     */
    public String getOnline_uuid() {
        return online_uuid;
    }

    /**
     * 获得当前name
     * @return 当前name
     */
    public String getCurrent_name() {
        return current_name;
    }

    /**
     * 设置当前name
     * @param current_name 新的name
     */
    public void setCurrent_name(String current_name) {
        this.current_name = current_name;
    }

    /**
     * 获得重定向的UUID字符串
     * @return 重定向的UUID字符串
     */
    public String getRedirect_uuid() {
        return redirect_uuid;
    }

    /**
     * 设置重定向的UUID字符串
     * @param redirect_uuid 新的UUID字符串
     */
    public void setRedirect_uuid(String redirect_uuid) {
        this.redirect_uuid = redirect_uuid;
    }

    /**
     * 获得验证的Yggdrasil服务器的path
     * @return Yggdrasil服务器的path
     */
    public String getYggdrasil_service() {
        return yggdrasil_service;
    }

    /**
     * 设置验证的Yggdrasil服务器的path
     * @param yggdrasil_service Yggdrasil的path
     */
    public void setYggdrasil_service(String yggdrasil_service) {
        this.yggdrasil_service = yggdrasil_service;
        this.serviceEntry = PluginData.getYggdrasilServerEntry(yggdrasil_service);
    }

    /**
     * 获得该数据是否有白名单
     * @return 是否有白名单
     */
    public int getWhitelist() {
        return whitelist;
    }

    /**
     * 设置该数据是否有白名单
     * @param whitelist 该数据是否有白名单
     */
    public void setWhitelist(int whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * 获得该数据的Yggdrasil服务器对象
     * @return Yggdrasil服务器对象
     */
    public YggdrasilServiceEntry getServiceEntry() {
        return serviceEntry;
    }

    @Override
    public String toString() {
        return "UserEntry{" +
                "online_uuid='" + online_uuid + '\'' +
                ", current_name='" + current_name + '\'' +
                ", redirect_uuid='" + redirect_uuid + '\'' +
                ", yggdrasil_service='" + yggdrasil_service + '\'' +
                ", whitelist=" + whitelist +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntry entry = (UserEntry) o;
        return Objects.equals(online_uuid, entry.online_uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(online_uuid);
    }

    /**
     * 通过一个数据库检索结果生成一个数据对象
     * @param resultSet 数据库检索结果
     * @return 数据对象
     */
    protected static UserEntry fromSQLResultSet(ResultSet resultSet) throws SQLException {
        return new UserEntry(
                resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getInt(5));
    }

    protected void writeNewUserEntryPreparedStatement(PreparedStatement ps) throws SQLException {
        ps.setString(1, online_uuid);
        ps.setString(2, current_name);
        ps.setString(3, redirect_uuid);
        ps.setString(4, yggdrasil_service);
        ps.setInt(5, whitelist);
    }

    protected void updateUserEntryPreparedStatement(PreparedStatement ps) throws SQLException {
        ps.setString(1, current_name);
        ps.setString(2, redirect_uuid);
        ps.setString(3, yggdrasil_service);
        ps.setInt(4, whitelist);
        ps.setString(5, online_uuid);
    }
}
