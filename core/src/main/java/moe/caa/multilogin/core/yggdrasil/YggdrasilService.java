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
import moe.caa.multilogin.core.main.MultiCore;
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
    private final MultiCore core;

    private YggdrasilService(String path, Boolean enable, String name, YggdrasilServiceBody body, ConvUuidEnum convUuid, Boolean convRepeat, String nameAllowedRegular, Boolean whitelist, Boolean refuseRepeatedLogin, Integer authRetry, MultiCore core) {
        this.path = ValueUtil.getOrThrow(path, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "path"));
        this.enable = ValueUtil.getOrThrow(enable, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "enable"));
        this.name = ValueUtil.getOrThrow(name, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "name"));
        this.body = ValueUtil.getOrThrow(body, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "body"));
        this.convUuid = ValueUtil.getOrThrow(convUuid, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "convUuid"));
        this.convRepeat = ValueUtil.getOrThrow(convRepeat, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "convRepeat"));
        this.nameAllowedRegular = ValueUtil.getOrThrow(nameAllowedRegular, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "nameAllowedRegular"));
        this.whitelist = ValueUtil.getOrThrow(whitelist, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "whitelist"));
        this.refuseRepeatedLogin = ValueUtil.getOrThrow(refuseRepeatedLogin, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "refuseRepeatedLogin"));
        this.authRetry = ValueUtil.getOrThrow(authRetry, LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "authRetry"));
        this.core = core;
        integrity();
    }

    public static YggdrasilService fromYamlConfig(String path, YamlConfig config, MultiCore core) {
        return new YggdrasilService(
                path,
                config.get("enable", Boolean.class),
                config.get("name", String.class),
                YggdrasilServiceBody.fromYaml(config.get("body", YamlConfig.class)),
                config.get("convUuid", ConvUuidEnum.class),
                config.get("convRepeat", Boolean.class),
                config.get("nameAllowedRegular", String.class),
                config.get("whitelist", Boolean.class),
                config.get("refuseRepeatedLogin", Boolean.class),
                config.get("authRetry", Integer.class), core
        );
    }

    /**
     * 验证配置完整性
     */
    private void integrity() {
        switch (ValueUtil.getOrThrow(body.getServerType(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "serverType"))) {
            case CUSTOM:
                ValueUtil.getOrThrow(body.isPostMode(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "postMode"));
                if (body.isPostMode())
                    ValueUtil.getOrThrow(body.getPostContent(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "postContent"));
            case BLESSING_SKIN:
                ValueUtil.getOrThrow(body.getUrl(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "url"));
            default:
                if (ValueUtil.getOrThrow(body.isPassIp(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "passIp"))) {
                    if (body.isPostMode()) {
                        ValueUtil.getOrThrow(body.getPassIpContentByPost(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "passIpContentByPost"));
                    } else {
                        ValueUtil.getOrThrow(body.getPassIpContent(), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage(core, "passIpContent"));
                    }
                }
        }
        try {
            MessageFormat.format(body.getUrl(), "", "", "");
        } catch (Exception exception) {
            throw new IllegalArgumentException(LanguageKeys.URL_ILLEGAL_FORMAT.getMessage(core, exception.getMessage()));
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
            return MessageFormat.format(body.getPostContent(), username, serverId, MessageFormat.format(body.getPassIpContentByPost(), ip));
        }
        return MessageFormat.format(body.getPostContent(), username, serverId, "");
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
