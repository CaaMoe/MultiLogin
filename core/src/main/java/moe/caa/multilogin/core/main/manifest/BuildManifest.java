package moe.caa.multilogin.core.main.manifest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Date;
import java.util.Locale;

import static moe.caa.multilogin.core.main.manifest.BuildType.FAST;
import static moe.caa.multilogin.core.main.manifest.BuildType.FINAL;

/**
 * Jar 包源数据阅读
 */
@NoArgsConstructor
@Getter
public class BuildManifest {
    private final String buildType = "@MultiLogin-Build-Type@";
    private final String buildTimestamp = "@Build-Timestamp@";
    private final String version = "@MultiLogin-Version@";

    public void read(MultiCore core) throws InterruptedException {
        BuildType type = BuildType.valueOf(buildType.toUpperCase(Locale.ROOT));
        Date date = new Date(Long.parseLong(buildTimestamp));
        if (type != FINAL) {
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#################################################");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#   您正在使用的版本为不受支持的自动构建版本");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#        可能会包含一些恶性 BUG");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#");
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#    Build Time: " + date);
            MultiLogger.getLogger().log(LoggerLevel.WARN, "#    Version : " + version);
            MultiLogger.getLogger().log(LoggerLevel.WARN, "################################################");
            if (type == FAST) return;
            MultiLogger.getLogger().log(LoggerLevel.WARN, "服务器将在 15 秒后继续启动");
            core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                core.getUpdater().check();
            });
            Thread.sleep(15 * 1000);
        }
    }
}
