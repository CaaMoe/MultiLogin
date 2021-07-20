/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.ConvUuidEnum
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
public enum ConvUuidEnum {

    /**
     * Yggdrasil验证服务器提供的UUID
     */
    DEFAULT,

    /**
     * 生成离线UUID（盗版UUID）
     */
    OFFLINE,

    /**
     * 随机UUID
     */
    RANDOM;

    /**
     * 获得 UUID
     *
     * @param onlineUuid 玩家在 Yggdrasil 的在线 UUID
     * @param name       玩家的 name
     * @return 生成结果
     */
    public UUID getResultUuid(UUID onlineUuid, String name) {
        if (this == DEFAULT) {
            return onlineUuid;
        }
        if (this == OFFLINE) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
        if (this == RANDOM) {
            return UUID.randomUUID();
        }
        return null;
    }
}