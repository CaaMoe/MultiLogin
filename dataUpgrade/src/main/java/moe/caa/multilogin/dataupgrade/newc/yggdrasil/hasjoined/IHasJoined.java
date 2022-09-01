package moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public interface IHasJoined {
    CommentedConfigurationNode toYaml() throws SerializationException;
}
