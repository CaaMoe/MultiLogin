package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import lombok.ToString;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Objects;

/**
 * 表示 Blessing 的 HasJoined 配置
 */
@ToString
public class BlessingSkinHasJoinedConfig implements HasJoinedConfig {
    private String apiRoot;

    @Override
    public void initValue(CommentedConfigurationNode node) throws ConfException {
        apiRoot = node.node("apiRoot").getString();
        if (ValueUtil.isEmpty(apiRoot))
            throw new ConfException("BlessingSkin is specified, but apiRoot is empty.");
        if (!apiRoot.endsWith("/")) {
            apiRoot = apiRoot.concat("/");
        }
    }

    @Override
    public String getUrl() {
        return apiRoot.concat("session")
                .concat("server")
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlessingSkinHasJoinedConfig that = (BlessingSkinHasJoinedConfig) o;
        return Objects.equals(apiRoot, that.apiRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiRoot);
    }
}
