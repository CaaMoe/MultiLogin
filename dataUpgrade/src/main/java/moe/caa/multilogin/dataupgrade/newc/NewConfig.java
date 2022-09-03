package moe.caa.multilogin.dataupgrade.newc;

import lombok.Getter;
import moe.caa.multilogin.dataupgrade.oldc.OldConfig;
import moe.caa.multilogin.dataupgrade.sql.Backend;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 表示一个新版配置
 */
@Getter
public class NewConfig {
    private final boolean debug;
    private final Backend s_backend;
    private final String s_ip;
    private final int s_port;
    private final String s_username;
    private final String s_password;
    private final String s_database;
    private final String s_tablePrefix;
    private final String s_connectUrl;

    public NewConfig(OldConfig config) {
        this.debug = false;
        this.s_backend = config.getS_backend();
        this.s_ip = config.getS_ip();
        this.s_port = config.getS_port();
        this.s_username = config.getS_username();
        this.s_password = config.getS_password();
        this.s_database = config.getS_database();
        this.s_tablePrefix = config.getS_database();
        this.s_connectUrl = "";
    }

    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("debug").set(debug);
        ret.node("sql").node("backend").set(s_backend.name());
        ret.node("sql").node("ip").set(s_ip);
        ret.node("sql").node("port").set(s_port);
        ret.node("sql").node("username").set(s_username);
        ret.node("sql").node("password").set(s_password);
        ret.node("sql").node("databases").set(s_database);
        ret.node("sql").node("tablePrefix").set(s_tablePrefix);
        ret.node("sql").node("connectUrl").set(s_connectUrl);
        return ret;
    }
}
