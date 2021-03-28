/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.data.YggdrasilServiceEntry
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.auth.AuthTask;
import moe.caa.multilogin.core.data.ConvUuid;
import moe.caa.multilogin.core.impl.IConfiguration;
import moe.caa.multilogin.core.util.I18n;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 表示Yggdrasil服务器对象
 */
public class YggdrasilServiceEntry {
    private final String path;
    private final String url;
    private final String head;
    private final boolean postMode;
    private boolean enable;
    private String name;
    private ConvUuid convUuid;
    private boolean whitelist;

    private YggdrasilServiceEntry(String path, String name, String oUrl, String head, ConvUuid convUuid, boolean whitelist, boolean enable, boolean checkUrl, boolean postMode, boolean noUrlDeal) {
        this.path = path;
        this.name = name;
        this.convUuid = convUuid;
        this.whitelist = whitelist;
        this.postMode = postMode;
        this.head = head == null ? "hasJoined?" : head;
        this.enable = enable;
//        url处理
        if (!oUrl.endsWith("minecraft") && !noUrlDeal) {
            this.url = oUrl + "/sessionserver/session/minecraft";
        } else {
            this.url = oUrl;
        }
        Logger log = MultiCore.getPlugin().getPluginLogger();
        if (enable) {
            if (checkUrl && !postMode) {
                MultiCore.getPlugin().runTaskAsyncLater(() -> {
                    String onlineName = checkUrl(oUrl);
                    if (onlineName == null) {
                        log.severe(I18n.getTransString("plugin_severe_invalid_distinguish_yggdrasil", path));
                    } else {
                        log.info(I18n.getTransString("plugin_loaded_distinguish_Yggdrasil", onlineName, path));
                    }
                }, 0L);
            } else {
                MultiCore.getPlugin().runTaskAsyncLater(() -> log.info(I18n.getTransString("plugin_loaded_distinguish_Yggdrasil_no_check", name, path)), 0);
            }
        }
    }

    /**
     * 通过path和configuration section生成一个Yggdrasil服务器对象
     *
     * @param path    Yggdrasil的path
     * @param section configuration
     * @return Yggdrasil对象，可能为空
     */
    public static YggdrasilServiceEntry fromYaml(String path, IConfiguration section) {
        if (section != null) {
            String name = section.getString("name");
            String url = section.getString("url");
            String convUuid = section.getString("convUuid");
            String head = section.getString("head");
            ConvUuid convUuidEnum;
            boolean whitelist = section.getBoolean("whitelist", true);;
            boolean postMode = section.getBoolean("postMode", false);
            boolean noUrlDeal = section.getBoolean("noUrlDeal", false);
            try {
                convUuidEnum = ConvUuid.valueOf(convUuid);
            } catch (Exception ignore) {
                return null;
            }
            if (!PluginData.isEmpty(name) && !PluginData.isEmpty(url)) {
                boolean enable = section.getBoolean("enable", false);
                boolean checkUrl = section.getBoolean("checkUrl", true);
                return new YggdrasilServiceEntry(path, name, url, head, convUuidEnum, whitelist, enable, checkUrl, postMode, noUrlDeal);
            }
        }
        return null;
    }

    /**
     * 检查该Yggdrasil的验证链接是否符合一般规定
     *
     * @return 该URL是否符合规定
     */
    private String checkUrl(String oUrl) {
        try {
            URL url = new URL(oUrl);
            JsonObject jo = (JsonObject) new JsonParser().parse(new InputStreamReader(url.openConnection().getInputStream()));
            return jo.get("meta").getAsJsonObject().get("serverName").getAsString();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 构建该GET请求链接
     *
     * @param arg GET的请求参数，为“hasJoined?username=%s&serverId=%s%s”
     * @return 请求链接
     */
    public String buildUrlStr(Map<String, String> arg) throws UnsupportedEncodingException {
        if (postMode) return url;
        return String.format("%s/%s", url, _buildUrlStr(arg));
    }

    //    构造URL参数的方法
    private String _buildUrlStr(Map<String, String> arg) throws UnsupportedEncodingException {
        if (arg == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> entry : arg.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            } else {
                sb.append(head);
            }
            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            if (entry.getValue() != null) {
                sb.append('=');
                sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        return sb.toString();
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public ConvUuid getConvUuid() {
        return convUuid;
    }

    public void setConvUuid(ConvUuid convUuid) {
        this.convUuid = convUuid;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isEnable() {
        return enable;
    }

    protected void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isPostMode() {
        return postMode;
    }
}
