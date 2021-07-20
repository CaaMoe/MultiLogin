/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.database.handler;

import moe.caa.multilogin.core.data.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static moe.caa.multilogin.core.data.database.SQLManager.CACHE_WHITELIST_TABLE_NAME;
import static moe.caa.multilogin.core.data.database.SQLManager.WHITELIST;

public class CacheWhitelistDataHandler {
    private final SQLManager sqlManager;

    public CacheWhitelistDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }


    public void init(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + CACHE_WHITELIST_TABLE_NAME + "( " +
                WHITELIST + "  VARCHAR(36) PRIMARY KEY NOT NULL)");
    }

    public boolean removeCacheWhitelist(String nameOrUuid) throws SQLException {
        try (Connection conn = sqlManager.getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("DELETE FROM %s WHERE %s = ?",
                CACHE_WHITELIST_TABLE_NAME, WHITELIST
        ))) {
            ps.setString(1, nameOrUuid);
            return ps.executeUpdate() != 0;
        }
    }

    public boolean addCacheWhitelist(String nameOrUuid) throws SQLException {
        try {
            try (Connection conn = sqlManager.getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s) VALUES(?)",
                    CACHE_WHITELIST_TABLE_NAME, WHITELIST
            ))) {
                ps.setString(1, nameOrUuid);
                return ps.executeUpdate() != 0;
            }
        } catch (SQLException ignore) {
            return false;
        }
    }
}
