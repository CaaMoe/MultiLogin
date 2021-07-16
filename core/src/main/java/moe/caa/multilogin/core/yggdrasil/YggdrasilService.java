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
}
