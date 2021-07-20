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
    private final UUID onlineUuid;
    private final transient YggdrasilService service;
    private String currentName;
    private UUID redirectUuid;
    private String yggdrasilService;
    private boolean whitelist;

    public User(UUID onlineUuid, String currentName, UUID redirectUuid, String yggdrasilService, boolean whitelist, YggdrasilServicesHandler servicesHandler) {
        this.onlineUuid = onlineUuid;
        this.currentName = currentName;
        this.redirectUuid = redirectUuid;
        this.yggdrasilService = yggdrasilService;
        this.whitelist = whitelist;
        this.service = servicesHandler.getService(yggdrasilService);
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

    public UUID getOnlineUuid() {
        return onlineUuid;
    }

    public YggdrasilService getService() {
        return service;
    }

    public String getCurrentName() {
        return currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public UUID getRedirectUuid() {
        return redirectUuid;
    }

    public void setRedirectUuid(UUID redirectUuid) {
        this.redirectUuid = redirectUuid;
    }

    public String getYggdrasilService() {
        return yggdrasilService;
    }

    public void setYggdrasilService(String yggdrasilService) {
        this.yggdrasilService = yggdrasilService;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }
}
