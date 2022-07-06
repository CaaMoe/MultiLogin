package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;

public class OfficialHasJoinedConfig implements HasJoinedConfig {

    @Override
    public void initValue(CommentedConfigurationNode node) {
    }

    @Override
    public String getUrl() {
        return "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}";
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
}
