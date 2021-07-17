package moe.caa.multilogin.core.data.database.handler;


import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.sql.*;
import java.util.*;

import static moe.caa.multilogin.core.data.database.SQLHandler.*;


public class UserDataHandler {

    public static void init(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + USER_DATA_TABLE_NAME + "( " +
                ONLINE_UUID + "  binary(16) PRIMARY KEY NOT NULL, " +
                CURRENT_NAME + "  VARCHAR(32), " +
                REDIRECT_UUID + "  binary(16), " +
                YGGDRASIL_SERVICE + "  VARCHAR(50), " +
                WHITELIST + "  INTEGER)");
    }

    public static User getUserEntryByOnlineUuid(UUID uuid) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, ONLINE_UUID
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(uuid));
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return fromSQLResultSet(resultSet);
            }
            return null;
        }
    }

    public static List<User> getUserEntryByRedirectUuid(UUID uuid) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, REDIRECT_UUID
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(uuid));
            ResultSet resultSet = ps.executeQuery();
            List<User> ret = new ArrayList<>();
            while (resultSet.next()) {
                User add = fromSQLResultSet(resultSet);
                ret.add(add);
            }
            return ret;
        }
    }

    public static List<User> getUserEntryByCurrentName(String name) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                USER_DATA_TABLE_NAME, CURRENT_NAME
        ))) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            List<User> ret = new ArrayList<>();
            while (resultSet.next()) {
                User add = fromSQLResultSet(resultSet);
                ret.add(add);
            }
            return ret;
        }
    }

    private static User fromSQLResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                ValueUtil.byteToUuid(resultSet.getBytes(1)),
                resultSet.getString(2),
                ValueUtil.byteToUuid(resultSet.getBytes(3)),
                resultSet.getString(4),
                resultSet.getInt(5) != 0);
    }

    public static Set<YggdrasilService> getYggdrasilServiceByCurrentName(String name) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT %s FROM %s WHERE %s = ?",
                YGGDRASIL_SERVICE, USER_DATA_TABLE_NAME, CURRENT_NAME
        ))) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            Set<YggdrasilService> ret = new HashSet<>();
            while (resultSet.next()) {
                ret.add(YggdrasilServicesHandler.getService(resultSet.getString(1)));
            }
            return ret;
        }
    }

    public static void writeNewUserEntry(User entry) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                USER_DATA_TABLE_NAME, ONLINE_UUID, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(entry.onlineUuid));
            ps.setString(2, entry.currentName);
            ps.setBytes(3, ValueUtil.uuidToByte(entry.redirectUuid));
            ps.setString(4, entry.yggdrasilService);
            ps.setInt(5, entry.whitelist ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public static void updateUserEntry(User entry) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST, ONLINE_UUID
        ))) {
            ps.setString(1, entry.currentName);
            ps.setBytes(2, ValueUtil.uuidToByte(entry.redirectUuid));
            ps.setString(3, entry.yggdrasilService);
            ps.setInt(4, entry.whitelist ? 1 : 0);
            ps.setBytes(5, ValueUtil.uuidToByte(entry.onlineUuid));
            ps.executeUpdate();
        }
    }
}
