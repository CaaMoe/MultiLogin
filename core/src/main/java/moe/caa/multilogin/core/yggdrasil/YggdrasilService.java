package moe.caa.multilogin.core.yggdrasil;

import lombok.Data;
import lombok.var;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * 代表 Yggdrasil 账户验证服务器数据对象
 */
@Data
public class YggdrasilService {
    private final String path;
    private final boolean enable;
    private final String name;
    private final String url;
    private final boolean postMode;
    private final boolean passIp;
    private final String passIpContent;
    private final String postContent;
    private final ConvUuidEnum convUuid;
    private final boolean convRepeat;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final int authRetry;
    private final SkinRestorerRuleEnum skinRestorer;
    private final int skinRestorerRetry;

    private YggdrasilService(String path, boolean enable, String name, String url, boolean postMode, boolean passIp,
                             String passIpContent, String postContent, ConvUuidEnum convUuid, boolean convRepeat,
                             String nameAllowedRegular, boolean whitelist, boolean refuseRepeatedLogin,
                             int authRetry, SkinRestorerRuleEnum skinRestorer, int skinRestorerRetry) {
        this.path = Objects.requireNonNull(path, "path is null");
        this.enable = enable;
        this.name = Objects.requireNonNull(name, "name is null");
        this.url = Objects.requireNonNull(url, "url is null");
        this.postMode = postMode;
        this.passIp = passIp;
        this.passIpContent = Objects.requireNonNull(passIpContent, "passIpContent is null");
        this.postContent = postContent;
        this.convUuid = Objects.requireNonNull(convUuid, "convUuid is null");
        this.convRepeat = convRepeat;
        this.nameAllowedRegular = Objects.requireNonNull(nameAllowedRegular, "nameAllowedRegular is null");
        this.whitelist = whitelist;
        this.refuseRepeatedLogin = refuseRepeatedLogin;
        this.authRetry = authRetry;
        this.skinRestorer = Objects.requireNonNull(skinRestorer, "skinRestorer is null");
        this.skinRestorerRetry = skinRestorerRetry;
    }

    /**
     * 通过配置节点构建这个对象
     *
     * @param path   标识符
     * @param config 配置文件节点
     * @return Yggdrasil 账户验证服务器数据对象
     */
    public static YggdrasilService getYggdrasilServiceFromMap(String path, YamlReader config) {
        YamlReader bodyConfig = config.get("body", YamlReader.class, YamlReader.empty());
        var enable = config.get("enable", Boolean.class, true);

        var name = config.get("name", String.class);

        var url = Objects.requireNonNull(bodyConfig.get("url", String.class), "name is null");
        var postMode = bodyConfig.get("postMode", Boolean.class, false);
        var passIp = bodyConfig.get("passIp", Boolean.class, false);
        var passIpContent = bodyConfig.get("passIpContent", String.class, "&ip={ip}");
        var postContent = bodyConfig.get("postContent", String.class, "{\"username\":\"{username}\", \"serverId\":\"{serverId}\"}");

        var convUuid = config.get("convUuid", ConvUuidEnum.class, ConvUuidEnum.DEFAULT);
        var convRepeat = config.get("convRepeat", Boolean.class, true);
        var nameAllowedRegular = config.get("nameAllowedRegular", String.class, "");
        var whitelist = config.get("whitelist", Boolean.class, false);
        var refuseRepeatedLogin = config.get("refuseRepeatedLogin", Boolean.class, false);
        var authRetry = config.get("authRetry", Number.class, 1).intValue();
        var skinRestorer = config.get("skinRestorer", SkinRestorerRuleEnum.class, SkinRestorerRuleEnum.OFF);
        var skinRestorerRetry = config.get("skinRestorerRetry", Number.class, 2).intValue();

        if (ValueUtil.isEmpty(url)) {
            throw new IllegalArgumentException("url is illegal argument");
        }

        if (ValueUtil.isEmpty(path)) {
            throw new IllegalArgumentException("path is illegal argument");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("url is illegal argument", e);
        }

        return new YggdrasilService(
                path, enable, name, url, postMode, passIp, passIpContent, postContent,
                convUuid, convRepeat, nameAllowedRegular, whitelist, refuseRepeatedLogin, authRetry,
                skinRestorer, skinRestorerRetry
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YggdrasilService that = (YggdrasilService) o;
        return Objects.equals(that.path, path);
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
        if (postMode) return url;
        if (passIp) {
            var passIp = buildPassIpContent(ip);
            return ValueUtil.format(url, FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("username").content(username).build(),
                    FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                    FormatContent.FormatEntry.builder().name("passIpContent").content(passIp).build()
            ));
        }
        return ValueUtil.format(url, FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("username").content(username).build(),
                FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                FormatContent.FormatEntry.builder().name("passIpContent").content("").build()
        ));
    }

    /**
     * 构建 ip 信息内容
     *
     * @param ip ip
     * @return ip 信息内容
     */
    private String buildPassIpContent(String ip) {
        var passIp = passIpContent;
        if (ValueUtil.isEmpty(passIp)) return "";
        if (ValueUtil.isEmpty(ip)) return "";
        return ValueUtil.format(passIp, FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("ip").content(ip).build()
        ));
    }

    /**
     * 构建 POST 请求报文内容
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       地址
     * @return URL
     */
    public String buildPostContent(String username, String serverId, String ip) {
        if (!postMode) return "";
        if (passIp) {
            var passIp = buildPassIpContent(ip);
            return ValueUtil.format(postContent, FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("username").content(username).build(),
                    FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                    FormatContent.FormatEntry.builder().name("passIpContent").content(passIp).build()
            ));
        }
        return ValueUtil.format(postContent, FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("username").content(username).build(),
                FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                FormatContent.FormatEntry.builder().name("passIpContent").content("").build()
        ));
    }


}
