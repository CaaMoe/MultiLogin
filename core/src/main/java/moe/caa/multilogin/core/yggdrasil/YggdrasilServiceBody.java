/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.yggdrasil.YggdrasilServiceBody
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.yggdrasil;

import moe.caa.multilogin.core.data.ServerTypeEnum;
import moe.caa.multilogin.core.util.YamlConfig;

/**
 * 表示 Yggdrasil Body
 */
public class YggdrasilServiceBody {
    private final ServerTypeEnum serverType;
    private final String url;
    private final Boolean postMode;
    private final Boolean passIp;
    private final String passIpContent;
    private final String postContent;
    private final String passIpContentByPost;

    private YggdrasilServiceBody(ServerTypeEnum serverType, String url, Boolean postMode, Boolean passIp, String passIpContent, String postContent, String passIpContentByPost) {
        this.serverType = serverType;
        this.url = serverType == ServerTypeEnum.MINECRAFT ?
                "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}" : serverType == ServerTypeEnum.BLESSING_SKIN ?
                url + "/sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}" : url;
        this.postMode = postMode;
        this.passIp = passIp;
        this.passIpContent = serverType != ServerTypeEnum.CUSTOM ? "ip={0}" : passIpContent;
        this.postContent = postContent;
        this.passIpContentByPost = passIpContentByPost;
    }

    protected static YggdrasilServiceBody fromYaml(YamlConfig config) {
        if (config == null) return null;
        return new YggdrasilServiceBody(
                config.get("serverType", ServerTypeEnum.class),
                config.get("url", String.class),
                config.get("postMode", Boolean.class),
                config.get("passIp", Boolean.class),
                config.get("passIpContent", String.class),
                config.get("postContent", String.class),
                config.get("passIpContentByPost", String.class)
        );
    }

    public ServerTypeEnum getServerType() {
        return serverType;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getPostMode() {
        return postMode;
    }

    public Boolean getPassIp() {
        return passIp;
    }

    public String getPassIpContent() {
        return passIpContent;
    }

    public String getPostContent() {
        return postContent;
    }

    public String getPassIpContentByPost() {
        return passIpContentByPost;
    }
}
