package moe.caa.multilogin.dataupgrade.newc;

import moe.caa.multilogin.dataupgrade.oldc.OldConfig;
import moe.caa.multilogin.dataupgrade.sql.Backend;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

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
        return ret;
    }
}
