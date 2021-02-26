package moe.caa.multilogin.core.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

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

    public String getOnline_uuid() {
        return online_uuid;
    }

    public String getCurrent_name() {
        return current_name;
    }

    public void setCurrent_name(String current_name) {
        this.current_name = current_name;
    }

    public String getRedirect_uuid() {
        return redirect_uuid;
    }

    public void setRedirect_uuid(String redirect_uuid) {
        this.redirect_uuid = redirect_uuid;
    }

    public String getYggdrasil_service() {
        return yggdrasil_service;
    }

    public void setYggdrasil_service(String yggdrasil_service) {
        this.yggdrasil_service = yggdrasil_service;
        this.serviceEntry = PluginData.getYggdrasilServerEntry(yggdrasil_service);
    }

    public int getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(int whitelist) {
        this.whitelist = whitelist;
    }

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
