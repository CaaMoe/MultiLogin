package moe.caa.multilogin.core.yggdrasil;

import moe.caa.multilogin.core.data.ServerTypeEnum;
import moe.caa.multilogin.core.util.YamlConfig;

public class YggdrasilServiceBody {
    public final ServerTypeEnum serverType;
    public final String url;
    public final Boolean postMode;
    public final Boolean passIp;
    public final String passIpContent;
    public final String postContent;
    public final String passIpContentByPost;

    private YggdrasilServiceBody(ServerTypeEnum serverType, String url, Boolean postMode, Boolean passIp, String passIpContent, String postContent, String passIpContentByPost) {
        this.serverType = serverType;
        this.url = serverType == ServerTypeEnum.MINECRAFT ?
                "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}" : serverType == ServerTypeEnum.BLESSING_SKIN ?
                url + "/sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}" : url;
        this.postMode = postMode;
        this.passIp = passIp;
        this.passIpContent = passIpContent;
        this.postContent = postContent;
        this.passIpContentByPost = passIpContentByPost;
    }

    public static YggdrasilServiceBody fromYaml(YamlConfig config) {
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
}
