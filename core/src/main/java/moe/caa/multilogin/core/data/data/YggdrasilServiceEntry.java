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

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.ConvUuid;
import moe.caa.multilogin.core.http.HttpGetter;
import moe.caa.multilogin.core.impl.IConfiguration;
import moe.caa.multilogin.core.util.I18n;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 表示Yggdrasil服务器对象
 */
public class YggdrasilServiceEntry {
    private final String path;
    private final ServerType serverType;
    private final String postContent;
    private final ConvUuid convUuid;
    private final String url;
    private final boolean postMode;
    private boolean enable;
    private String name;
    private boolean checkUrl;
    private boolean whitelist;
    private boolean skinRepair;
    private int skinRepairRetry;
    private int authRetry;

    public YggdrasilServiceEntry(String path, boolean enable, String name, ServerType serverType, String url,
                                 boolean postMode, boolean checkUrl, String postContent, ConvUuid convUuid,
                                 boolean whitelist, boolean skinRepair, int skinRepairRetry, int authRetry) throws Exception {
        String url1;
        String postContent1;
        this.path = Optional.ofNullable(path).map(s -> PluginData.isEmpty(s) ? null : s).orElseThrow(() -> new IllegalArgumentException("path"));
        this.name = Optional.ofNullable(name).map(s -> PluginData.isEmpty(s) ? null : s).orElseThrow(() -> new IllegalArgumentException("name"));
        this.serverType = Optional.ofNullable(serverType).orElseThrow(() -> new IllegalArgumentException("serverType"));
        this.convUuid = Optional.ofNullable(convUuid).orElseThrow(() -> new IllegalArgumentException("convUuid"));
        this.enable = enable;
        url1 = url;
        this.postMode = postMode;
        this.checkUrl = checkUrl;
        postContent1 = postContent;
        this.whitelist = whitelist;
        this.skinRepair = skinRepair;
        this.skinRepairRetry = skinRepairRetry;
        this.authRetry = authRetry;

        switch (serverType) {
            case CUSTOM:
                postContent1 = Optional.ofNullable(postContent).map(s -> PluginData.isEmpty(s) ? null : s).orElseThrow(() -> new IllegalArgumentException("postContent"));
            case BLESSING_SKIN:
                url1 = Optional.ofNullable(url).map(s -> PluginData.isEmpty(s) ? null : s + "/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s").orElseThrow(() -> new IllegalArgumentException("url"));
                break;
            case MINECRAFT:
                url1 = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s";
        }
        this.url = url1;
        this.postContent = postContent1;

        complete();
    }

    /**
     * 通过path和configuration section生成一个Yggdrasil服务器对象<br><br>
     * <p>
     * 结构：<br>
     * &emsp;                   path:<br>
     * &emsp;&emsp;               enable:<br>
     * &emsp;&emsp;               body:<br>
     * &emsp;&emsp;&emsp;           name:<br>
     * &emsp;&emsp;&emsp;           serverType:<br>
     * &emsp;&emsp;&emsp;           url:<br>
     * &emsp;&emsp;&emsp;           postMode:<br>
     * &emsp;&emsp;&emsp;           postContent:<br>
     * &emsp;&emsp;               checkUrl:<br>
     * &emsp;&emsp;               convUuid:<br>
     * &emsp;&emsp;               whitelist:<br>
     * &emsp;&emsp;               skinRepair:<br>
     * &emsp;&emsp;               skinRepairRetry:<br>
     * &emsp;&emsp;               authRetry:<br>
     *
     * @param path    Yggdrasil的path
     * @param section configuration
     * @return Yggdrasil对象
     */
    public static YggdrasilServiceEntry fromYaml(String path, IConfiguration section) throws Exception {
        IConfiguration body = section.getConfigurationSection("body");
        boolean enable = section.getBoolean("enable", false);
        String name = body.getString("name");
        ServerType serverType = ServerType.valueOf(body.getString("serverType"));
        String url = null;
        boolean postMode = false;
        String postContent = null;
        switch (serverType) {
            case CUSTOM:
                postContent = body.getString("postContent");
                postMode = body.getBoolean("postMode");
            case BLESSING_SKIN:
                url = body.getString("url");
        }
        boolean checkUrl = section.getBoolean("checkUrl");
        ConvUuid convUuid = ConvUuid.valueOf(section.getString("convUuid"));
        boolean whitelist = section.getBoolean("whitelist", true);
        boolean skinRepair = section.getBoolean("skinRepair", false);
        int skinRepairRetry = (int) section.getLong("skinRepairRetry", 3);
        int authRetry = (int) section.getLong("authRetry", 1);
        return new YggdrasilServiceEntry(path, enable, name, serverType, url, postMode, checkUrl, postContent, convUuid, whitelist, skinRepair, skinRepairRetry, authRetry);
    }

    private void complete() {
        Logger log = MultiCore.getPlugin().getPluginLogger();
        if (enable) {
            if (checkUrl) {
                MultiCore.getPlugin().runTaskAsyncLater(() -> {
                    try {
                        if (postMode) {
                            HttpGetter.httpPost(url, String.format(postContent, "test", "test"), authRetry);
                        } else {
                            Map<String, String> arg = new HashMap<>();
                            arg.put("username", "test");
                            arg.put("serverId", "test");
                            HttpGetter.httpGet(buildUrlStr(arg));
                        }
                        log.info(I18n.getTransString("plugin_loaded_distinguish_Yggdrasil", name, path));
                    } catch (Exception e) {
                        log.info(I18n.getTransString("plugin_severe_invalid_distinguish_yggdrasil", name, path));
                    }
                }, 0);
            } else {
                MultiCore.getPlugin().runTaskAsyncLater(() -> log.info(I18n.getTransString("plugin_loaded_distinguish_Yggdrasil_no_check", name, path)), 0);
            }
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public String getPath() {
        return path;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPostMode() {
        return postMode;
    }

    public boolean isCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(boolean checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getPostContent() {
        return postContent;
    }

    public ConvUuid getConvUuid() {
        return convUuid;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isSkinRepair() {
        return skinRepair;
    }

    public void setSkinRepair(boolean skinRepair) {
        this.skinRepair = skinRepair;
    }

    public int getSkinRepairRetry() {
        return skinRepairRetry;
    }

    public void setSkinRepairRetry(int skinRepairRetry) {
        this.skinRepairRetry = skinRepairRetry;
    }

    public int getAuthRetry() {
        return authRetry;
    }

    public void setAuthRetry(int authRetry) {
        this.authRetry = authRetry;
    }

    /**
     * 构建该GET请求链接
     *
     * @param arg GET的请求参数，为“hasJoined?username=%s&serverId=%s%s”
     * @return 请求链接
     */
    public String buildUrlStr(Map<String, String> arg) {
        if (postMode) return url;
        if (arg == null || arg.size() != 2) {
            return "";
        }
        String username = arg.get("username");
        String serverId = arg.get("serverId");
        return String.format(url, username, serverId);
    }

    public enum ServerType {

        /**
         * 基于Blessing Skin设计的Yggdrasil验证服务器
         */
        BLESSING_SKIN,

        /**
         * 原版官方的正版验证服务器
         */
        MINECRAFT,

        /**
         * 其他自定义服务器
         */
        CUSTOM;
    }
}
