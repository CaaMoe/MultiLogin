package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * 非稳定版本输出Banner
 */

@Getter
public class BuildManifest {
    private final MultiCore core;
    private String buildType;
    private Date buildDate;
    private String version;

    public BuildManifest(MultiCore core) {
        this.core = core;
    }

    public void read() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/build.properties"));

        buildType = properties.getProperty("build_type");
        buildDate = new Date(Long.parseLong(properties.getProperty("build_timestamp")));
        version = properties.getProperty("version");
    }

    public void checkStable() {
        if (!buildType.equalsIgnoreCase("final")) {
            LoggerProvider.getLogger().warn("######################################################");
            LoggerProvider.getLogger().warn("#   Warning, you are not using a stable version");
            LoggerProvider.getLogger().warn("# and may have some very fatal errors!");
            LoggerProvider.getLogger().warn("#");
            LoggerProvider.getLogger().warn("#   Please download the latest stable version");
            LoggerProvider.getLogger().warn("# from https://github.com/CaaMoe/MultiLogin/releases");
            LoggerProvider.getLogger().warn("#");
            LoggerProvider.getLogger().warn("#     Build Time : " + buildDate);
            LoggerProvider.getLogger().warn("#     Version    : " + version);
            LoggerProvider.getLogger().warn("######################################################");
        }
    }
}
