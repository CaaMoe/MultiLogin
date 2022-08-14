package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

/**
 * 表示高度自定义的 HasJoined 配置
 */
@ToString
public class CustomHasJoinedConfig implements HasJoinedConfig {
    @Getter
    private String url;
    @Getter
    private YggdrasilServiceConfig.HttpRequestMethod method;
    @Getter
    private String ipContent;
    @Getter
    private String postContent;

    @Override
    public void initValue(CommentedConfigurationNode node) throws ConfException, SerializationException {
        url = node.node("url").getString();
        method = node.node("method").get(YggdrasilServiceConfig.HttpRequestMethod.class);
        ipContent = node.node("ipContent").getString();
        postContent = node.node("postContent").getString();

        if (ValueUtil.isEmpty(url))
            throw new ConfException("Custom is specified, but url is empty.");
        if (method == null)
            throw new ConfException("Custom is specified, but method is empty.");
        if (method == YggdrasilServiceConfig.HttpRequestMethod.POST && ValueUtil.isEmpty(postContent))
            throw new ConfException("HTTP POST request is specified, but the request content is empty.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomHasJoinedConfig that = (CustomHasJoinedConfig) o;
        return Objects.equals(url, that.url) && method == that.method && Objects.equals(ipContent, that.ipContent) && Objects.equals(postContent, that.postContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method, ipContent, postContent);
    }
}
