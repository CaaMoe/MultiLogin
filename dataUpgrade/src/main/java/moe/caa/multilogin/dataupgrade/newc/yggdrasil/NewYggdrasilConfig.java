package moe.caa.multilogin.dataupgrade.newc.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined.Custom;
import moe.caa.multilogin.dataupgrade.newc.yggdrasil.hasjoined.IHasJoined;
import moe.caa.multilogin.dataupgrade.oldc.OldConfig;
import moe.caa.multilogin.dataupgrade.oldc.OldYggdrasilConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 新格式配置文件
 */
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


    public NewYggdrasilConfig(
            int id, OldConfig config, OldYggdrasilConfig oldYggdrasilConfig
    ) {

        this.id = id;
        this.name = oldYggdrasilConfig.getName();
        this.hasJoined = getHasJoined(oldYggdrasilConfig);
        this.passIp = oldYggdrasilConfig.isB_passIp();
        this.timeout = config.getServicesTimeOut();
        this.retry = oldYggdrasilConfig.getAuthRetry();
        this.retryDelay = 0;
        this.proxy = new Proxy();
        this.initUUID = InitUUID.valueOf(oldYggdrasilConfig.getConvUuid().name());
        this.nameAllowedRegular = oldYggdrasilConfig.getNameAllowedRegular();
        this.whitelist = oldYggdrasilConfig.isWhitelist();
        this.refuseRepeatedLogin = oldYggdrasilConfig.isRefuseRepeatedLogin();
        this.compulsoryUsername = false;
        this.skinRestorer = new SkinRestorer(config, oldYggdrasilConfig);
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
        ret.node("initUUID").set(initUUID.name());
        ret.node("nameAllowedRegular").set(nameAllowedRegular);
        ret.node("whitelist").set(whitelist);
        ret.node("refuseRepeatedLogin").set(refuseRepeatedLogin);
        ret.node("compulsoryUsername").set(compulsoryUsername);
        ret.node("skinRestorer").set(skinRestorer.toYaml());
        return ret;
    }

    private IHasJoined getHasJoined(OldYggdrasilConfig oldYggdrasilConfig) {

        return new Custom(oldYggdrasilConfig.getB_url(),
                oldYggdrasilConfig.isB_postMode() ? Custom.HttpMethod.POST : Custom.HttpMethod.GET,
                oldYggdrasilConfig.getB_passIpContent(),
                oldYggdrasilConfig.getB_postContent()
        );
    }

    public enum InitUUID {
        DEFAULT, OFFLINE, RANDOM;
    }
}
