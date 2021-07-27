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

import moe.caa.multilogin.core.util.YamlConfig;

/**
 * 表示 Yggdrasil Body
 */
public class YggdrasilServiceBody {
    private final String url;
    private final boolean postMode;
    private final boolean passIp;
    private final String passIpContent;
    private final String postContent;
    private final String passIpContentByPost;

    private YggdrasilServiceBody(String url, boolean postMode, boolean passIp, String passIpContent, String postContent, String passIpContentByPost) {
        this.url = url;
        this.postMode = postMode;
        this.passIp = passIp;
        this.passIpContent = passIpContent;
        this.postContent = postContent;
        this.passIpContentByPost = passIpContentByPost;
    }

    protected static YggdrasilServiceBody fromYaml(YamlConfig config) {
        if (config == null) return null;
        return new YggdrasilServiceBody(
                config.get("url", String.class),
                config.get("postMode", Boolean.class, false),
                config.get("passIp", Boolean.class, false),
                config.get("passIpContent", String.class),
                config.get("postContent", String.class),
                config.get("passIpContentByPost", String.class)
        );
    }


    public String getUrl() {
        return url;
    }

    public boolean isPostMode() {
        return postMode;
    }

    public boolean isPassIp() {
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
