package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class SqlConfig {
    private final SqlBackend backend;
    private final String ip;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final String tablePrefix;
    private final String connectUrl;

    public enum SqlBackend {
        H2, MYSQL
    }
}
