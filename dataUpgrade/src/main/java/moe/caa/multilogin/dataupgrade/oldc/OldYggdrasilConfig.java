package moe.caa.multilogin.dataupgrade.oldc;


import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;

@Getter
@ToString
public class OldYggdrasilConfig {
    private final String path;
    private final boolean enable;
    private final String name;

    private final String b_url;
    private final boolean b_postMode;
    private final boolean b_passIp;
    private final String b_passIpContent;
    private final String b_postContent;

    private final ConvUUID convUuid;
    private final boolean convRepeat;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final int authRetry;
    private final SkinRestorer skinRestorer;
    private final int skinRestorerRetry;

    @SneakyThrows
    protected OldYggdrasilConfig(String path, CommentedConfigurationNode node) {
        this.path = path;
        this.enable = node.node("enable").getBoolean(true);
        this.name = node.node("name").getString();

        {
            CommentedConfigurationNode body = node.node("body");
            this.b_url = body.node("url").getString();
            this.b_postMode = body.node("postMode").getBoolean(false);
            this.b_passIp = body.node("passIp").getBoolean(false);
            this.b_passIpContent = body.node("passIpContent").getString("&ip={ip}");
            this.b_postContent = body.node("postContent").getString("{\"username\":\"{username}\", \"serverId\":\"{serverId}\"}");
        }

        this.convUuid = node.node("convUuid").get(ConvUUID.class, ConvUUID.DEFAULT);
        this.convRepeat = node.node("convRepeat").getBoolean(true);
        this.nameAllowedRegular = node.node("nameAllowedRegular").getString("");
        this.whitelist = node.node("whitelist").getBoolean(false);
        this.refuseRepeatedLogin = node.node("refuseRepeatedLogin").getBoolean(false);
        this.authRetry = node.node("authRetry").getInt(1);

        Object sro = node.node("skinRestorer").raw();
        if (sro instanceof Boolean) {
            if (!(Boolean) sro) {
                this.skinRestorer = SkinRestorer.OFF;
            } else {
                this.skinRestorer = node.node("skinRestorer").get(SkinRestorer.class, SkinRestorer.OFF);
            }
        } else {
            this.skinRestorer = node.node("skinRestorer").get(SkinRestorer.class, SkinRestorer.OFF);
        }

        this.skinRestorerRetry = node.node("skinRestorerRetry").getInt(2);
    }

    public enum ConvUUID {
        DEFAULT, OFFLINE, RANDOM;
    }

    public enum SkinRestorer {
        OFF, LOGIN, ASYNC
    }
}