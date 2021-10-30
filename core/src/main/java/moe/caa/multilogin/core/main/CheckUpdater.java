package moe.caa.multilogin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 检查更新程序
 */
public class CheckUpdater {
    private final URL source;
    public Version latestVersion = null;
    public Version currentVersion = null;
    private MultiCore core;

    public CheckUpdater(MultiCore core) throws MalformedURLException {
        this.core = core;
        source = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
        try {
            currentVersion = Version.fromString(core.getPlugin().getPluginVersion().trim());
        } catch (Exception ignore) {
        }
    }

    /**
     * 检查更新
     */
    public Version checkVersion() throws Exception {
        try {
            JsonObject json = JsonParser.parseString(HttpUtil.httpGet(source, 10000, 3)).getAsJsonObject();
            String sor = json.get("content").getAsString();
            sor = sor.substring(0, sor.length() - 1);
            String s = new String(ValueUtil.getDECODER().decode(sor), StandardCharsets.UTF_8).split("=")[1].trim();
            return Version.fromString(s);
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Failed to check updates.", e);
            throw e;
        }
    }

    public boolean shouldUpdate() throws Exception {
        latestVersion = checkVersion();
        if (latestVersion == null) return false;
        return currentVersion == null || currentVersion.shouldUpdate(latestVersion);
    }

    public void check() {
        try {
            if (shouldUpdate()) {
                MultiLogger.getLogger().log(LoggerLevel.INFO, "联网检查的最新版本为 " + latestVersion + "， 当前版本为 " + core.getPlugin().getPluginVersion() + ", 请注意及时更新。");
            }
        } catch (Exception ignored) {
        }
    }
}