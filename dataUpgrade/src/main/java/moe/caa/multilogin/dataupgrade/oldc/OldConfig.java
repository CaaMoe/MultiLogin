package moe.caa.multilogin.dataupgrade.oldc;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import moe.caa.multilogin.dataupgrade.sql.Backend;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 老的 config.yml 文件
 */
@Getter
@ToString
public class OldConfig {
    private final List<OldYggdrasilConfig> services;
    private final int servicesTimeOut;
    private final boolean whitelist;
    private final String nameAllowedRegular;

    private final Backend s_backend;
    private final String s_ip;
    private final int s_port;
    private final String s_username;
    private final String s_password;
    private final String s_database;
    private final String s_prefix;

    @SneakyThrows
    public OldConfig(CommentedConfigurationNode load){
        this.services = load.node("services").childrenMap().entrySet().stream()
                .map(e -> new OldYggdrasilConfig(((String) e.getKey()), e.getValue())).collect(Collectors.toList());

        this.servicesTimeOut = load.node("servicesTimeOut").getInt(10000);
        this.whitelist = load.node("whitelist").getBoolean(true);
        this.nameAllowedRegular = load.node("nameAllowedRegular").getString("^[0-9a-zA-Z_]{1,16}$");

        CommentedConfigurationNode sql = load.node("sql");
        this.s_backend = sql.node("backend").get(Backend.class, Backend.H2);
        this.s_ip = sql.node("ip").getString("127.0.0.1");
        this.s_port = sql.node("port").getInt(3306);
        this.s_username = sql.node("username").getString("root");
        this.s_password = sql.node("password").getString("12345");
        this.s_database = sql.node("database").getString("multilogin");
        this.s_prefix = sql.node("prefix").getString("multilogin");
    }


    @Getter
    @ToString
    public static class OldYggdrasilConfig {
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
        private OldYggdrasilConfig(String path, CommentedConfigurationNode node)  {
            this.path = path;
            this.enable = node.node("enable").getBoolean(true);
            this.name = node.node("name").getString();

            {
                CommentedConfigurationNode body = node.node("body");
                this.b_url = body.node( "url").getString();
                this.b_postMode = body.node( "postMode").getBoolean(false);
                this.b_passIp = body.node( "passIp").getBoolean(false);
                this.b_passIpContent = body.node( "passIpContent").getString("&ip={ip}");
                this.b_postContent = body.node( "postContent").getString("{\"username\":\"{username}\", \"serverId\":\"{serverId}\"}");
            }

            this.convUuid = node.node("convUuid").get(ConvUUID.class, ConvUUID.DEFAULT);
            this.convRepeat = node.node("convRepeat").getBoolean(true);
            this.nameAllowedRegular = node.node("nameAllowedRegular").getString("");
            this.whitelist = node.node("whitelist").getBoolean(false);
            this.refuseRepeatedLogin = node.node("refuseRepeatedLogin").getBoolean(false);
            this.authRetry = node.node("authRetry").getInt(1);

            Object sro = node.node("skinRestorer").raw();
            if(sro instanceof Boolean){
                if(!(Boolean) sro){
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

        public enum SkinRestorer{
            OFF, LOGIN, ASYNC
        }
    }
}
