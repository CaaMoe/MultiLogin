/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.data.UserEntry
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.data;

import java.util.Objects;
import java.util.UUID;

/**
 * 表示数据库中存放的玩家对象
 */
public class UserEntry {

    private final UUID online_uuid;
    private String current_name;
    private UUID redirect_uuid;
    private String yggdrasil_service;
    private int whitelist;
    private transient YggdrasilServiceEntry serviceEntry;

    public UserEntry(UUID online_uuid, String current_name, UUID redirect_uuid, String yggdrasil_service, int whitelist) {
        this.online_uuid = online_uuid;
        this.current_name = current_name;
        this.redirect_uuid = redirect_uuid;
        this.yggdrasil_service = yggdrasil_service;
        this.whitelist = whitelist;
        this.serviceEntry = PluginData.getYggdrasilServerEntry(yggdrasil_service);
    }


    /**
     * 获得在线UUID
     *
     * @return 在线UUID
     */
    public UUID getOnline_uuid() {
        return online_uuid;
    }

    /**
     * 获得当前name
     *
     * @return 当前name
     */
    public String getCurrent_name() {
        return current_name;
    }

    /**
     * 设置当前name
     *
     * @param current_name 新的name
     */
    public void setCurrent_name(String current_name) {
        this.current_name = current_name;
    }

    /**
     * 获得重定向的UUID字符串
     *
     * @return 重定向的UUID字符串
     */
    public UUID getRedirect_uuid() {
        return redirect_uuid;
    }

    /**
     * 设置重定向的UUID字符串
     *
     * @param redirect_uuid 新的UUID字符串
     */
    public void setRedirect_uuid(UUID redirect_uuid) {
        this.redirect_uuid = redirect_uuid;
    }

    /**
     * 获得验证的Yggdrasil服务器的path
     *
     * @return Yggdrasil服务器的path
     */
    public String getYggdrasil_service() {
        return yggdrasil_service;
    }

    /**
     * 设置验证的Yggdrasil服务器的path
     *
     * @param yggdrasil_service Yggdrasil的path
     */
    public void setYggdrasil_service(String yggdrasil_service) {
        this.yggdrasil_service = yggdrasil_service;
        this.serviceEntry = PluginData.getYggdrasilServerEntry(yggdrasil_service);
    }

    /**
     * 获得该数据是否有白名单
     *
     * @return 是否有白名单
     */
    public int getWhitelist() {
        return whitelist;
    }

    /**
     * 设置该数据是否有白名单
     *
     * @param whitelist 该数据是否有白名单
     */
    public void setWhitelist(int whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * 获得该数据的Yggdrasil服务器对象
     *
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
}
