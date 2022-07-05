package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class YggdrasilServiceConfig {
    private final int id;
    private final String name;

    private final String url;
    private final HttpRequestMethod method;
    private final String ipContent;
    private final String postContent;
    private final boolean passIp;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final ProxyConfig proxy;

    private final InitUUID initUUID;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final boolean compulsoryUsername;
    private final SkinRestorerConfig skinRestorer;

    public enum HttpRequestMethod {
        GET, POST
    }

    public enum InitUUID {
        DEFAULT, OFFLINE, RANDOM
    }

    public static YggdrasilServiceConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
        int id = node.node("id").getInt();
        String name = node.node("name").getString("Unnamed");

        String url = null;
        HttpRequestMethod method = null;
        String ipContent = null;
        String postContent = null;

        boolean read = false;
        if (node.node("hasJoined").hasChild("official")) {
            url = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}";
            method = HttpRequestMethod.GET;
            ipContent = "&ip={0}";
            postContent = "";
            read = true;
        }
        if (node.node("hasJoined").hasChild("blessingSkin")) {
            if (read) throw new ConfException("There can only be one, official, blessingSkin and custom.");
            String s = Objects.requireNonNull(node.node("hasJoined", "blessingSkin", "apiRoot").getString());
            if (!s.endsWith("/")) {
                s = s.concat("/");
            }
            url = s.concat("sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}");
            method = HttpRequestMethod.GET;
            ipContent = "&ip={0}";
            postContent = "";
            read = true;
        }
        if (node.node("hasJoined").hasChild("custom")) {
            if (read) throw new ConfException("There can only be one, official, blessingSkin and custom.");
            url = node.node("hasJoined", "custom", "url").getString();
            method = node.node("hasJoined", "custom", "method").get(HttpRequestMethod.class);
            ipContent = node.node("hasJoined", "custom", "ipContents").getString();
            postContent = node.node("hasJoined", "custom", "postContent").getString();
            read = true;
        }
        boolean passIp = node.node("passIp").getBoolean(true);
        int timeout = node.node("timeout").getInt(10000);
        int retry = node.node("retry").getInt(0);
        int retryDelay = node.node("retryDelay").getInt(0);
        ProxyConfig proxy = ProxyConfig.read(node.node("proxy"));

        InitUUID initUUID = node.node("initUUID").get(InitUUID.class, InitUUID.DEFAULT);
        String nameAllowedRegular = node.node("nameAllowedRegular").getString("^[0-9a-zA-Z_]{3,16}$");
        boolean whitelist = node.node("whitelist").getBoolean(false);
        boolean refuseRepeatedLogin = node.node("refuseRepeatedLogin").getBoolean(false);
        boolean compulsoryUsername = node.node("compulsoryUsername").getBoolean(false);
        SkinRestorerConfig skinRestorer = SkinRestorerConfig.read(node.node("skinRestorer"));

        return new YggdrasilServiceConfig(id, name, url, method, ipContent, postContent, passIp, timeout, retry, retryDelay, proxy, initUUID, nameAllowedRegular, whitelist, refuseRepeatedLogin, compulsoryUsername, skinRestorer);
    }
}
