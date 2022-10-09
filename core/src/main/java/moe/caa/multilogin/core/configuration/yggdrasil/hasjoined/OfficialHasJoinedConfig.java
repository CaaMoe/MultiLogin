package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import lombok.ToString;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;

/**
 * 表示 MoJang 官方的 HasJoined 配置
 */
@ToString
public class OfficialHasJoinedConfig implements HasJoinedConfig {

    @Override
    public void initValue(CommentedConfigurationNode node) {
    }

    @Override
    public String getUrl() {
        return "https://".concat("session")
                .concat("server.")
                .concat("mojang")
                .concat(".com")
                .concat("/session")
                .concat("/minecraft")
                .concat("/hasJoined?")
                .concat("username={0}&serverId={1}{2}");
    }

    @Override
    public YggdrasilServiceConfig.HttpRequestMethod getMethod() {
        return YggdrasilServiceConfig.HttpRequestMethod.GET;
    }

    @Override
    public String getIpContent() {
        return "&ip={0}";
    }

    @Override
    public String getPostContent() {
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OfficialHasJoinedConfig;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
