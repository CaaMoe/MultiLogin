/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.databse.SQLHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.databse;

import moe.caa.multilogin.core.data.databse.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.databse.handler.TexturesDataHandler;
import moe.caa.multilogin.core.data.databse.handler.UserDataHandler;
import moe.caa.multilogin.core.data.databse.pool.AbstractConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库管理类
 */
public class SQLHandler {
    public static final String USER_DATA_TABLE_NAME = "user_data";
    public static final String ONLINE_UUID = "online_uuid";
    public static final String CURRENT_NAME = "current_name";
    public static final String REDIRECT_UUID = "redirect_name";
    public static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    public static final String WHITELIST = "whitelist";
    public static final String CACHE_WHITELIST_TABLE_NAME = "whitelist";
    public static final String REPAIR_SKIN_TABLE_NAME = "repair_skin";
    public static final String PROPERTY = "textures";
    public static final String REPAIR_PROPERTY = "repair_textures";
    public static AbstractConnectionPool pool;

    /**
     * 通过参数链接到数据库
     *
     * @param connectionPool 连接池
     */
    public static void init(AbstractConnectionPool connectionPool) throws Exception {
        pool = connectionPool;
        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            UserDataHandler.init(s);
            CacheWhitelistDataHandler.init(s);
            TexturesDataHandler.init(s);
        }
    }

    public static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    /**
     * 关闭连接池
     */
    public static void close() throws SQLException {
        if(pool != null){
            pool.close();
        }
    }
}
