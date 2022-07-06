package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import lombok.Getter;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;

public class BlessingSkinHasJoinedConfig implements HasJoinedConfig {
    @Getter
    private String url;
    @Getter
    private YggdrasilServiceConfig.HttpRequestMethod method;
    @Getter
    private String ipContent;
    @Getter
    private String postContent;

    @Override
    public void initValue(CommentedConfigurationNode node) throws ConfException {
        String s = node.node("apiRoot").getString();
        if (ValueUtil.isEmpty(s))
            throw new ConfException("BlessingSkin is specified, but apiRoot is empty.");
        if (!s.endsWith("/")) {
            s = s.concat("/");
        }
        url = s.concat("sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}");
        method = YggdrasilServiceConfig.HttpRequestMethod.GET;
        ipContent = "&ip={0}";
        postContent = "";
    }
}
