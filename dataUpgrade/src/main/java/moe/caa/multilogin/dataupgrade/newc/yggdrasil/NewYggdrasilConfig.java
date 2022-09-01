package moe.caa.multilogin.dataupgrade.newc.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined.IHasJoined;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

@Getter
@ToString
public class NewYggdrasilConfig {
    private final int id;
    private final String name;

    private final IHasJoined hasJoined;

    private final boolean passIp;
    private final int timeout;
    private final int retry;
    private final int retryDelay;

    private final Proxy proxy;

    private final InitUUID initUUID;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final boolean compulsoryUsername;

    private final SkinRestorer skinRestorer;


    private NewYggdrasilConfig(int id, String name, IHasJoined hasJoined, boolean passIp, int timeout, int retry, int retryDelay, Proxy proxy, InitUUID initUUID, String nameAllowedRegular, boolean whitelist, boolean refuseRepeatedLogin, boolean compulsoryUsername, SkinRestorer skinRestorer) {
        this.id = id;
        this.name = name;
        this.hasJoined = hasJoined;
        this.passIp = passIp;
        this.timeout = timeout;
        this.retry = retry;
        this.retryDelay = retryDelay;
        this.proxy = proxy;
        this.initUUID = initUUID;
        this.nameAllowedRegular = nameAllowedRegular;
        this.whitelist = whitelist;
        this.refuseRepeatedLogin = refuseRepeatedLogin;
        this.compulsoryUsername = compulsoryUsername;
        this.skinRestorer = skinRestorer;
    }

    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("id").set(id);
        ret.node("name").set(name);
        ret.node("hasJoined").set(hasJoined.toYaml());
        ret.node("passIp").set(passIp);
        ret.node("timeout").set(timeout);
        ret.node("retry").set(retry);
        ret.node("retryDelay").set(retryDelay);
        ret.node("proxy").set(proxy.toYaml());
        ret.node("initUUID").set(initUUID);
        ret.node("nameAllowedRegular").set(nameAllowedRegular);
        ret.node("whitelist").set(whitelist);
        ret.node("refuseRepeatedLogin").set(refuseRepeatedLogin);
        ret.node("compulsoryUsername").set(compulsoryUsername);
        ret.node("skinRestorer").set(skinRestorer.toYaml());
        return ret;
    }

    public enum InitUUID {
        DEFAULT, OFFLINE, RANDOM;
    }
}
