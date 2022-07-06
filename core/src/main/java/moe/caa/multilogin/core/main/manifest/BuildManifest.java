package moe.caa.multilogin.core.main.manifest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@NoArgsConstructor
@Getter
public class BuildManifest {
    public void read(MultiCore core) throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/build.properties"));

        String build_type = properties.getProperty("build_type");
        Date date = new Date(Long.parseLong(properties.getProperty("build_timestamp")));

        if (!build_type.equalsIgnoreCase("final")) {
            LoggerProvider.getLogger().warn("######################################################");
            LoggerProvider.getLogger().warn("#   Warning, you are not using a stable version");
            LoggerProvider.getLogger().warn("# and may have some very fatal errors!");
            LoggerProvider.getLogger().warn("#");
            LoggerProvider.getLogger().warn("#   Please download the latest stable version");
            LoggerProvider.getLogger().warn("# from https://github.com/CaaMoe/MultiLogin/releases");
            LoggerProvider.getLogger().warn("#");
            LoggerProvider.getLogger().warn("#     Build Time : " + date);
            LoggerProvider.getLogger().warn("#     Version    : " + core.getPlugin().getVersion());
            LoggerProvider.getLogger().warn("######################################################");
        }
    }
}
