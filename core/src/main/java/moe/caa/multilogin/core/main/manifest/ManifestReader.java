package moe.caa.multilogin.core.main.manifest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.IOUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static moe.caa.multilogin.core.main.manifest.BuildType.FAST;
import static moe.caa.multilogin.core.main.manifest.BuildType.FINAL;

/**
 * Jar 包源数据阅读
 */
@NoArgsConstructor
@Getter
public class ManifestReader {
    public void read() throws IOException, InterruptedException {
        Attributes attributes = new Manifest(IOUtil.getJarResource("META-INF/MANIFEST.MF")).getMainAttributes();
        BuildType type = BuildType.valueOf(attributes.getValue("MultiLogin-Build-Type").toUpperCase(Locale.ROOT));
        if (type != FINAL) {
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#################################################");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#   您正在使用的版本并不是稳定的(预)发布版本");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#        可能会包含一些恶性 BUG");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#    Build Time: " + new Date((Long.parseLong(attributes.getValue("Build-Timestamp")))));
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#    Version : " + attributes.getValue("MultiLogin-Version"));
            MultiLogger.getLogger().log(LoggerLevel.WARN, "################################################");
            if (type == FAST) return;
            MultiLogger.getLogger().log(LoggerLevel.WARN, "服务端将在 7 秒钟后继续启动");
            Thread.sleep(7 * 1000);
        }
    }
}
