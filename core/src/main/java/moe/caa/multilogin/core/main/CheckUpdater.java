/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.main.CheckUpdater
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 检查更新程序
 */
public class CheckUpdater {
    private static final URL SOURCE = HttpUtil.getUrlFromString("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
    public final MultiCore core;
    public String latestVersion = null;
    public boolean haveUpdate = false;

    public CheckUpdater(MultiCore core) {
        this.core = core;
    }

    /**
     * 检查更新
     */
    private void check0() throws Exception {
        // JsonObject json = JsonParser.parseString(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        JsonObject json = new JsonParser().parse(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        String sor = json.get("content").getAsString();
        sor = sor.substring(0, sor.length() - 1);
        String s = new String(ValueUtil.DECODER.decode(sor), StandardCharsets.UTF_8);
        latestVersion = s.split("=")[1].trim();
    }

    public void check() {
        try {
            check0();
        } catch (Exception ignored) {
        }
        if (!haveUpdate) {
            haveUpdate = true;
            core.getLogger().log(LoggerLevel.INFO, LanguageKeys.UPDATE_CONSOLE.getMessage(core, core.plugin.getPluginVersion(), latestVersion));
        }
    }
}
