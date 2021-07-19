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
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlConfig;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 表示 Yggdrasil 验证服务器对象
 */
public class YggdrasilService {
    public final String path;
    public final Boolean enable;
    public final String name;
    public final YggdrasilServiceBody body;
    public final Boolean checkUrl;
    public final ConvUuidEnum convUuid;
    public final Boolean convRepeat;
    public final String nameAllowedRegular;
    public final Boolean whitelist;
    public final Integer authRetry;

    public YggdrasilService(String path, Boolean enable, String name, YggdrasilServiceBody body, Boolean checkUrl, ConvUuidEnum convUuid, Boolean convRepeat, String nameAllowedRegular, Boolean whitelist, Integer authRetry) {
        this.path = ValueUtil.getOrThrow(path, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("path"));
        this.enable = ValueUtil.getOrThrow(enable, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("enable"));
        this.name = ValueUtil.getOrThrow(name, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("name"));
        this.body = ValueUtil.getOrThrow(body, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("body"));
        this.checkUrl = ValueUtil.getOrThrow(checkUrl, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("checkUrl"));
        this.convUuid = ValueUtil.getOrThrow(convUuid, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("convUuid"));
        this.convRepeat = ValueUtil.getOrThrow(convRepeat, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("convRepeat"));
        this.nameAllowedRegular = ValueUtil.getOrThrow(nameAllowedRegular, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("nameAllowedRegular"));
        this.whitelist = ValueUtil.getOrThrow(whitelist, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("whitelist"));
        this.authRetry = ValueUtil.getOrThrow(authRetry, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("authRetry"));
        integrity();
    }

    public static YggdrasilService fromYamlConfig(String path, YamlConfig config) {
        return new YggdrasilService(
                path,
                config.get("enable", Boolean.class),
                config.get("name", String.class),
                YggdrasilServiceBody.fromYaml(config.get("body", YamlConfig.class)),
                config.get("checkUrl", Boolean.class),
                config.get("convUuid", ConvUuidEnum.class),
                config.get("convRepeat", Boolean.class),
                config.get("nameAllowedRegular", String.class),
                config.get("whitelist", Boolean.class),
                config.get("authRetry", Integer.class)
        );
    }

    /**
     * 验证配置完整性
     */
    private void integrity() {
        switch (ValueUtil.getOrThrow(body.serverType, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("serverType"))) {
            case CUSTOM:
                ValueUtil.getOrThrow(body.postMode, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("postMode"));
                if (body.postMode)
                    ValueUtil.getOrThrow(body.postContent, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("postContent"));
            case BLESSING_SKIN:
                ValueUtil.getOrThrow(body.url, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("url"));
            default:
                if (ValueUtil.getOrThrow(body.passIp, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIp"))) {
                    if (body.postMode) {
                        ValueUtil.getOrThrow(body.passIpContentByPost, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIpContentByPost"));
                    } else {
                        ValueUtil.getOrThrow(body.passIpContent, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("passIpContent"));
                    }
                }
        }
        try {
            MessageFormat.format(body.url, "", "", "");
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
        if (body.postMode) return body.url;
        if (body.passIp && ValueUtil.notIsEmpty(ip)) {
            return MessageFormat.format(body.url, username, serverId, MessageFormat.format(body.passIpContent, ip));
        }
        return MessageFormat.format(body.url, username, serverId, "");
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
        if (!body.postMode) return null;
        if (body.passIp && ValueUtil.notIsEmpty(ip)) {
            return MessageFormat.format(body.postContent, username, serverId, MessageFormat.format(body.passIpContentByPost, ip));
        }
        return MessageFormat.format(body.postContent, username, serverId, "");
    }
}
