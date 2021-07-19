/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.User
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data;

import moe.caa.multilogin.core.yggdrasil.YggdrasilService;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.util.Objects;
import java.util.UUID;

public class User {
    public final UUID onlineUuid;
    public final transient YggdrasilService service;
    public String currentName;
    public UUID redirectUuid;
    public String yggdrasilService;
    public boolean whitelist;

    public User(UUID onlineUuid, String currentName, UUID redirectUuid, String yggdrasilService, boolean whitelist) {
        this.onlineUuid = onlineUuid;
        this.currentName = currentName;
        this.redirectUuid = redirectUuid;
        this.yggdrasilService = yggdrasilService;
        this.whitelist = whitelist;
        this.service = YggdrasilServicesHandler.getService(yggdrasilService);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User entry = (User) o;
        return Objects.equals(onlineUuid, entry.onlineUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlineUuid);
    }
}
