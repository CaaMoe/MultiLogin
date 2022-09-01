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
}
