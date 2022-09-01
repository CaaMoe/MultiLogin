package moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class Official implements IHasJoined {
    @Override
    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("official").set(null);
        return ret;
    }
}
