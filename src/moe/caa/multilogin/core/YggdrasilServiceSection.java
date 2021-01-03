package moe.caa.multilogin.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class YggdrasilServiceSection {
    public static YggdrasilServiceSection OFFICIAL;

    private final String path;
    private final String name;
    private final String url;
    private final YggdrasilServiceSection.ConvUuid convUuid;
    private final boolean whitelist;

    public YggdrasilServiceSection(String path, String name, String url, YggdrasilServiceSection.ConvUuid convUuid,boolean whitelist, boolean logger) {
        this.name = name;
        this.url = url;
        this.convUuid = convUuid;
        this.path = path;
        this.whitelist = whitelist;
        if(logger){
            Logger log = MultiCore.getPlugin().getMLPluginLogger();
            log.info(String.format("添加Yggdrasil验证服务器: %s, URL: %s", name, url));
        }

    }

    public static YggdrasilServiceSection fromYaml(String path, IConfiguration section){
        if (section != null){
            String name = section.getString("name");
            String url = section.getString("url");
            String convUuid = section.getString("convUuid");
            YggdrasilServiceSection.ConvUuid convUuidEnum = null;
            boolean whitelist = true;
            try {
                convUuidEnum = YggdrasilServiceSection.ConvUuid.valueOf(convUuid);
                whitelist = section.getBoolean("whitelist");
            } catch (Exception ignore){
            }
            if(!PluginData.isEmpty(name) && !PluginData.isEmpty(url) && convUuidEnum != null){
                return new YggdrasilServiceSection(path, name, url, convUuidEnum,whitelist, true);
            }
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public ConvUuid getConvUuid() {
        return convUuid;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean checkUrl(){
        try {
            URL url = new URL(this.url);
            return url.openConnection().getInputStream().available() > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    public URL buildUrl(String arg) throws MalformedURLException {
        return new URL(url + "/sessionserver/session/minecraft/" + arg);
    }

    public enum ConvUuid{
        DEFAULT,
        OFFLINE;
    }
}
