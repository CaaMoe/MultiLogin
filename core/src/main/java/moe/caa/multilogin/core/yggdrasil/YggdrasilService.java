package moe.caa.multilogin.core.yggdrasil;

import lombok.Data;
import lombok.var;
import moe.caa.multilogin.core.data.ConvUuidEnum;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * 代表 Yggdrasil 账户验证服务器数据对象
 */

@Data
public class YggdrasilService {
    private final char[] path;
    private final boolean enable;
    private final char[] name;
    private final char[] url;
    private final boolean postMode;
    private final boolean passIp;
    private final char[] passIpContent;
    private final char[] postContent;
    private final ConvUuidEnum convUuid;
    private final boolean convRepeat;
    private final char[] nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final int authRetry;
    private final boolean safeId;

    /**
     * 构建这个 YggdrasilService 实例
     *
     * @param path                识别路径
     * @param enable              启用情况
     * @param name                别称
     * @param url                 请求链接
     * @param postMode            报文请求模式
     * @param passIp              是否传递 IP
     * @param passIpContent       传递的 IP 内容
     * @param postContent         报文请求内容
     * @param convUuid            UUID 生成规则
     * @param convRepeat          始终处理 UUID 重复问题
     * @param nameAllowedRegular  允许的用户名正则
     * @param whitelist           白名单
     * @param refuseRepeatedLogin 不进行强制登入
     * @param authRetry           验证超时重试次数
     * @param safeId              ID 强优先
     */
    private YggdrasilService(char[] path, boolean enable, char[] name, char[] url,
                             boolean postMode, boolean passIp, char[] passIpContent,
                             char[] postContent, ConvUuidEnum convUuid, boolean convRepeat,
                             char[] nameAllowedRegular, boolean whitelist, boolean refuseRepeatedLogin,
                             int authRetry, boolean safeId) {
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
        this.safeId = safeId;
    }

    public static YggdrasilService getYggdrasilServiceFromMap(String path, YamlConfig config) {
        YamlConfig bodyConfig = config.get("body", YamlConfig.class, YamlConfig.empty());
        var enable = config.get("enable", Boolean.class, true);

        var name = Objects.requireNonNull(config.get("name", String.class), "name is null");

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
        var authRetry = config.get("authRetry", Integer.class, 1);
        var safeId = config.get("safeId", Boolean.class, false);

        if (ValueUtil.isEmpty(url)) {
            throw new IllegalArgumentException("url is illegal argument");
        }

        if (ValueUtil.isEmpty(Objects.requireNonNull(path, "path is null"))) {
            throw new IllegalArgumentException("path is illegal argument");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("url is illegal argument", e);
        }

        return new YggdrasilService(
                path.toCharArray(), enable, name.toCharArray(),
                url.toCharArray(), postMode, passIp, passIpContent.toCharArray(), postContent.toCharArray(),
                convUuid, convRepeat, nameAllowedRegular.toCharArray(), whitelist, refuseRepeatedLogin, authRetry, safeId
        );
    }

    /**
     * 获得路径字符串
     *
     * @return 路径字符串
     */
    public String getPathString() {
        return ValueUtil.charArrayToString(path);
    }

    /**
     * 获得别称字符串
     *
     * @return 别称字符串
     */
    public String getNameString() {
        return ValueUtil.charArrayToString(name);
    }

    /**
     * 获得链接字符串
     *
     * @return 链接字符串
     */
    public String getUrlString() {
        return ValueUtil.charArrayToString(url);
    }

    /**
     * 获得报文内容字符串
     *
     * @return 报文内容字符串
     */
    public String getPostContentString() {
        return ValueUtil.charArrayToString(postContent);
    }

    /**
     * 获得 ip 信息内容字符串
     *
     * @return ip 信息内容字符串
     */
    public String getPassIpContentString() {
        return ValueUtil.charArrayToString(passIpContent);
    }

    /**
     * 获得用户名正则字符串
     *
     * @return 用户名正则字符串
     */
    public String getNameAllowedRegularString() {
        return ValueUtil.charArrayToString(nameAllowedRegular);
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
        if (postMode) return getUrlString();
        if (passIp) {
            var passIp = buildPassIpContent(ip);
            return ValueUtil.format(getUrlString(), new FormatContent(
                    FormatContent.FormatEntry.builder().name("username").content(username).build(),
                    FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                    FormatContent.FormatEntry.builder().name("passIpContent").content(passIp).build()
            ));
        }
        return ValueUtil.format(getUrlString(), new FormatContent(
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
        var passIp = getPassIpContentString();
        if (ValueUtil.isEmpty(passIp)) return "";
        return ValueUtil.format(passIp, new FormatContent(
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
    private String buildPostContent(String username, String serverId, String ip) {
        if (!postMode) return "";
        if (passIp) {
            var passIp = buildPassIpContent(ip);
            return ValueUtil.format(getPostContentString(), new FormatContent(
                    FormatContent.FormatEntry.builder().name("username").content(username).build(),
                    FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                    FormatContent.FormatEntry.builder().name("passIpContent").content(passIp).build()
            ));
        }
        return ValueUtil.format(getPostContentString(), new FormatContent(
                FormatContent.FormatEntry.builder().name("username").content(username).build(),
                FormatContent.FormatEntry.builder().name("serverId").content(serverId).build(),
                FormatContent.FormatEntry.builder().name("passIpContent").content("").build()
        ));
    }
}
