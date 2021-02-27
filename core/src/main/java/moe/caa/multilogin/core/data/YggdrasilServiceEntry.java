package moe.caa.multilogin.core.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.MultiCore;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * 表示Yggdrasil服务器对象
 */
public class YggdrasilServiceEntry {

    /**
     * 正版验证服务器对象
     */
    public static final YggdrasilServiceEntry OFFICIAL_YGG = new YggdrasilServiceEntry("official", "", "", null, false, false){
        @Override
        public String buildUrlStr(String arg) {
            return String.format("https://sessionserver.mojang.com/session/minecraft/%s", arg);
        }
    };
    private final String path;
    private boolean enable;
    private String name;
    private final String url;
    private ConvUuid convUuid;
    private boolean whitelist;

    private YggdrasilServiceEntry(String path, String name, String url, ConvUuid convUuid, boolean whitelist, boolean logger) {
        this.path = path;
        this.name = name;
        this.url = url;
        this.convUuid = convUuid;
        this.whitelist = whitelist;
        if(logger){
            Logger log = MultiCore.getPlugin().getPluginLogger();
            MultiCore.getPlugin().runTaskAsyncLater(()->{
                String onlineName = checkUrl();
                if(onlineName == null){
                    log.severe(String.format("无法识别的Yggdrasil验证服务器: %s (仍然会应用到现有列表中)", name));
                } else {
                    log.info(String.format("添加Yggdrasil验证服务器: %s, 来自节点: %s", onlineName, name));
                }
            }, 0L);
        }
    }

    /**
     * 检查该Yggdrasil的验证链接是否符合一般规定
     * @return 该URL是否符合规定
     */
    private String checkUrl(){
        try {
            URL url = new URL(this.url);
            JsonObject jo = (JsonObject) new JsonParser().parse(new InputStreamReader(url.openConnection().getInputStream()));
            return jo.get("meta").getAsJsonObject().get("serverName").getAsString();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 构建该GET请求链接
     * @param arg GET的请求参数，为“hasJoined?username=%s&serverId=%s%s”
     * @return 请求链接
     */
    public String buildUrlStr(String arg) {
        return String.format("%s/sessionserver/session/minecraft/%s", url, arg);
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
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

    public boolean isEnable() {
        return enable;
    }

    /**
     * 通过path和configuration section生成一个Yggdrasil服务器对象
     * @param path Yggdrasil的path
     * @param section configuration
     * @return Yggdrasil对象，可能为空
     */
    public static YggdrasilServiceEntry fromYaml(String path, IConfiguration section){
        if(section != null){
            String name = section.getString("name");
            String url = section.getString("url");
            String convUuid = section.getString("convUuid");
            ConvUuid convUuidEnum;
            boolean whitelist;
            try {
                convUuidEnum = ConvUuid.valueOf(convUuid);
                whitelist = section.getBoolean("whitelist");
            } catch (Exception ignore){
                return null;
            }
            if(!PluginData.isEmpty(name) && !PluginData.isEmpty(url)){
                boolean enable = section.getBoolean("enable");
                YggdrasilServiceEntry ret = new YggdrasilServiceEntry(path, name, url, convUuidEnum, whitelist, enable);
                ret.enable = enable;
                return ret;
            }
        }
        return null;
    }
}
