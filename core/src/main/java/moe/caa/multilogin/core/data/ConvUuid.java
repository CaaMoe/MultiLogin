/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.ConvUuid
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * UUID生成规则的枚举
 */
public enum ConvUuid {

    /**
     * Yggdrasil验证服务器提供的UUID
     */
    DEFAULT,

    /**
     * 生成离线UUID（盗版UUID）
     */
    OFFLINE;

    /**
     * 获得UUID
     *
     * @param onlineUuid 玩家在Yggdrasil的在线UUID
     * @param name       玩家的name
     * @return 生成结果
     */
    public UUID getResultUuid(UUID onlineUuid, String name) {
        if (this == DEFAULT) {
            return onlineUuid;
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}