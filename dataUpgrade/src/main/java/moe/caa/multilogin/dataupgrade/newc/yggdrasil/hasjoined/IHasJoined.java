package moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * HasJoined 节点
 */
public interface IHasJoined {

    /**
     * 导出配置片段
     */
    CommentedConfigurationNode toYaml() throws SerializationException;
}
