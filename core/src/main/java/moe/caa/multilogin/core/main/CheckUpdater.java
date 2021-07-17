package moe.caa.multilogin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 检查更新程序
 */
public class CheckUpdater {
    private static final URL SOURCE = HttpUtil.getUrlFromString("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
    private static String latestVersion = null;

    /**
     * 检查更新
     *
     * @return 获取的最新版本号
     */
    public static String check() throws Exception {
        JsonObject json = JsonParser.parseString(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        String sor = json.get("content").getAsString();
        sor = sor.substring(0, sor.length() - 1);
        String s = new String(ValueUtil.DECODER.decode(sor), StandardCharsets.UTF_8);
        latestVersion = s.split("=")[1].trim();
        return latestVersion;
    }
}
