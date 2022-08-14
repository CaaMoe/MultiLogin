package moe.caa.multilogin.core.configuration.yggdrasil.hasjoined;

import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 表示一个 HasJoined 配置
 */
public interface HasJoinedConfig {
    Map<String, Supplier<HasJoinedConfig>> hasJoinedSupplier = Map.of(
            "official", OfficialHasJoinedConfig::new,
            "blessingSkin", BlessingSkinHasJoinedConfig::new,
            "custom", CustomHasJoinedConfig::new);

    /**
     * 返回 HasJoined 配置
     */
    static HasJoinedConfig getHasJoinedConfig(CommentedConfigurationNode node) throws ConfException, SerializationException {
        HasJoinedConfig hasJoinedConfig = null;
        for (Map.Entry<String, Supplier<HasJoinedConfig>> entry : hasJoinedSupplier.entrySet()) {
            if (!node.hasChild(entry.getKey())) continue;
            if (hasJoinedConfig != null) {
                String message = "There can only be one, "
                        .concat(ValueUtil.join(", ", " and ", hasJoinedSupplier.keySet())).concat(".");
                throw new ConfException(message);
            }
            hasJoinedConfig = entry.getValue().get();
            hasJoinedConfig.initValue(node.node(entry.getKey()));
        }
        if (hasJoinedConfig == null) {
            String message = "Cannot find any of "
                    .concat(ValueUtil.join(", ", " or ", hasJoinedSupplier.keySet()
                            .stream().map(e ->
                                    String.join(".", Arrays.stream(node.path().array()).map(Object::toString)
                                            .collect(Collectors.joining(".")), e)
                            ).collect(Collectors.toList())
                    )).concat(".");
            throw new ConfException(message);
        }
        return hasJoinedConfig;
    }

    /**
     * 初始化值
     */
    void initValue(CommentedConfigurationNode node) throws ConfException, SerializationException;

    String getUrl();

    YggdrasilServiceConfig.HttpRequestMethod getMethod();

    String getIpContent();

    String getPostContent();
}
