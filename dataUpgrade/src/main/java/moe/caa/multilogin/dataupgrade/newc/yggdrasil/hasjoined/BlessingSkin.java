package moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class BlessingSkin implements IHasJoined {
    private final String apiRoot;

    public BlessingSkin(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("blessingSkin").node("apiRoot").set(apiRoot);
        return ret;
    }
}
