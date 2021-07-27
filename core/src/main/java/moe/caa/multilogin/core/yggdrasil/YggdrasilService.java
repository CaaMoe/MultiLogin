/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.yggdrasil.YggdrasilService
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.yggdrasil;

import moe.caa.multilogin.core.data.ConvUuidEnum;
import moe.caa.multilogin.core.data.ServerTypeEnum;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlConfig;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 表示 Yggdrasil 验证服务器对象
 */
public class YggdrasilService {
    private final String path;
    private final boolean enable;
    private final String name;
    private final YggdrasilServiceBody body;
    private final ConvUuidEnum convUuid;
    private final boolean convRepeat;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final int authRetry;

    private YggdrasilService(String path, boolean enable, String name, YggdrasilServiceBody body, ConvUuidEnum convUuid, Boolean convRepeat, String nameAllowedRegular, boolean whitelist, boolean refuseRepeatedLogin, Integer authRetry) {
        this.path = ValueUtil.getOrThrow(path, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("path"));
        this.enable = enable;
        this.name = ValueUtil.getOrThrow(name, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("name"));
        this.body = ValueUtil.getOrThrow(body, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("body"));
        this.convUuid = ValueUtil.getOrThrow(convUuid, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("convUuid"));
        this.convRepeat = ValueUtil.getOrThrow(convRepeat, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("convRepeat"));
        this.nameAllowedRegular = ValueUtil.getOrThrow(nameAllowedRegular, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("nameAllowedRegular"));
        this.whitelist = whitelist;
        this.refuseRepeatedLogin =refuseRepeatedLogin;
        this.authRetry = ValueUtil.getOrThrow(authRetry, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("authRetry"));
        integrity();
    }

    public static YggdrasilService fromYamlConfig(String path, YamlConfig config) {
        return new YggdrasilService(
                path,
                config.get("enable", Boolean.class, false),
                config.get("name", String.class),
                YggdrasilServiceBody.fromYaml(config.get("body", YamlConfig.class)),
                config.get("convUuid", ConvUuidEnum.class),
                config.get("convRepeat", Boolean.class),
                config.get("nameAllowedRegular", String.class),
                config.get("whitelist", Boolean.class, true),
                config.get("refuseRepeatedLogin", Boolean.class, false),
                config.get("authRetry", Integer.class)
        );
    }

    /**
     * 验证配置完整性
     */
    private void integrity() {
        if (ValueUtil.getOrThrow(body.getServerType(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("serverType")) == ServerTypeEnum.CUSTOM) {
            ValueUtil.getOrThrow(body.isPostMode(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("postMode"));
            if (body.isPostMode())
                ValueUtil.getOrThrow(body.getPostContent(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("postContent"));
        }
        ValueUtil.getOrThrow(body.getUrl(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("url"));
        if (ValueUtil.getOrThrow(body.isPassIp(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIp"))) {
            if (body.isPostMode()) {
                ValueUtil.getOrThrow(body.getPassIpContentByPost(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIpContentByPost"));
            } else {
                ValueUtil.getOrThrow(body.getPassIpContent(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIpContent"));
            }
        }
        try {
            MessageFormat.format(body.getUrl(), "", "", "");
        } catch (Exception exception) {
            throw new IllegalArgumentException(LanguageKeys.URL_ILLEGAL_FORMAT.getMessage(exception.getMessage()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YggdrasilService service = (YggdrasilService) o;
        return Objects.equals(path, service.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    /**
     * 构建 GET 请求 URL
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       地址
     * @return URL
     */
    public String buildUrl(String username, String serverId, String ip) {
        if (body.isPostMode()) return body.getUrl();
        if (body.isPassIp() && ValueUtil.notIsEmpty(ip)) {
            return MessageFormat.format(body.getUrl(), username, serverId, MessageFormat.format(body.getPassIpContent(), ip));
        }
        return MessageFormat.format(body.getUrl(), username, serverId, "");
    }

    /**
     * 构建 POST 请求内容
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       地址
     * @return 内容
     */
    public String buildPostContent(String username, String serverId, String ip) {
        if (!body.isPostMode()) return null;
        if (body.isPassIp() && ValueUtil.notIsEmpty(ip)) {
            return body.getPostContent().replace("{0}", username).replace("{1}", serverId).replace("{2}", MessageFormat.format(body.getPassIpContentByPost(), ip));
        }
        return body.getPostContent().replace("{0}", username).replace("{1}", serverId).replace("{2}", "");
    }

    public String getPath() {
        return path;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getName() {
        return name;
    }

    public ConvUuidEnum getConvUuid() {
        return convUuid;
    }

    public boolean isConvRepeat() {
        return convRepeat;
    }

    public String getNameAllowedRegular() {
        return nameAllowedRegular;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public int getAuthRetry() {
        return authRetry;
    }

    public YggdrasilServiceBody getBody() {
        return body;
    }

    public boolean isRefuseRepeatedLogin() {
        return refuseRepeatedLogin;
    }
}
