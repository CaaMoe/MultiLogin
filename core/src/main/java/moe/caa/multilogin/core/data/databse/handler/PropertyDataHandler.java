/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.databse.handler.PropertyDataHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.databse.handler;

import moe.caa.multilogin.core.data.data.UserProperty;
import moe.caa.multilogin.core.util.I18n;
import moe.caa.multilogin.core.util.UUIDSerializer;

import java.sql.*;
import java.util.UUID;

import static moe.caa.multilogin.core.data.databse.SQLHandler.*;

public class PropertyDataHandler {

    public static void init(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + REPAIR_SKIN_TABLE_NAME + "( " +
                ONLINE_UUID + "  binary(16) PRIMARY KEY NOT NULL, " +
                PROPERTY + " TEXT, " +
                REPAIR_PROPERTY + " TEXT)");
    }


    public static UserProperty getUserPropertyByOnlineUuid(UUID uuid) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                REPAIR_SKIN_TABLE_NAME, ONLINE_UUID
        ))) {
            ps.setBytes(1, UUIDSerializer.uuidToByte(uuid));
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                try {
                    UserProperty ret = new UserProperty();
                    ret.setOnlineUuid(uuid);
                    String[] args = resultSet.getString(2).split("\\s+");
                    ret.setProperty(new UserProperty.Property(args[0], args[1]));
                    args = resultSet.getString(3).split("\\s+");
                    ret.setRepair_property(new UserProperty.Property(args[0], args[1]));
                    return ret;
                } catch (Exception e) {
                    throw new RuntimeException(I18n.getTransString("plugin_severe_database_select_by_online_uuid", uuid.toString()), e);
                }
            }
            return null;
        }
    }

    public static void updateUserProperty(UserProperty userProperty) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ? limit 1",
                REPAIR_SKIN_TABLE_NAME, PROPERTY, REPAIR_PROPERTY, ONLINE_UUID
        ))) {
            ps.setString(1, userProperty.getProperty().getValue() + " " + userProperty.getProperty().getSignature());
            ps.setString(2, userProperty.getRepair_property().getValue() + " " + userProperty.getRepair_property().getSignature());
            ps.setBytes(3, UUIDSerializer.uuidToByte(userProperty.getOnlineUuid()));
            ps.executeUpdate();
        }
    }

    public static void writeNewUserProperty(UserProperty userProperty) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)",
                REPAIR_SKIN_TABLE_NAME, ONLINE_UUID, PROPERTY, REPAIR_PROPERTY
        ))) {
            ps.setBytes(1, UUIDSerializer.uuidToByte(userProperty.getOnlineUuid()));
            ps.setString(2, userProperty.getProperty().getValue() + " " + userProperty.getProperty().getSignature());
            ps.setString(3, userProperty.getRepair_property().getValue() + " " + userProperty.getRepair_property().getSignature());
            ps.executeUpdate();
        }
    }
}
