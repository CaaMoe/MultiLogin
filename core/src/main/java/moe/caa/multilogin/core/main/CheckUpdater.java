package moe.caa.multilogin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 检查更新程序
 */
public class CheckUpdater {
    private static final URL SOURCE = HttpUtil.getUrlFromString("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
    public static String latestVersion = null;
    public static boolean haveUpdate = false;

    /**
     * 检查更新
     */
    private static void check0() throws Exception {
        // JsonObject json = JsonParser.parseString(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        JsonObject json = new JsonParser().parse(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        String sor = json.get("content").getAsString();
        sor = sor.substring(0, sor.length() - 1);
        String s = new String(ValueUtil.DECODER.decode(sor), StandardCharsets.UTF_8);
        latestVersion = s.split("=")[1].trim();
    }

    public static void check() {
        try {
            check0();
        } catch (Exception ignored) {
        }
        if (!haveUpdate) {
            haveUpdate = true;
            MultiLogger.log(LoggerLevel.INFO, LanguageKeys.UPDATE_CONSOLE.getMessage(MultiCore.plugin.getPluginVersion(), latestVersion));
        }
    }
}
