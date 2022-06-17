package moe.caa.multilogin.core.configuration.yggdrasil;

import lombok.*;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class YggdrasilServiceConfig {
    private final int id;
    private final String name;
    private final HasJoinedConfig hasJoined;
    private final TransformUUID transformUUID;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final boolean compulsoryUsername;
    private final SkinRestorerConfig skinRestorer;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @ToString
    public static class HasJoinedConfig {
        private final String url;
        private final HttpRequestMethod method;
        private final boolean passIp;
        private final String ipContent;
        private final String postContent;
        private final int timeout;
        private final int retry;
        private final int retryDelay;
        private final ProxyConfig proxy;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @ToString
    public static class SkinRestorerConfig {
        private final SkinRestorerType restorer;
        private final SkinRestorerMethod method;
        private final int timeout;
        private final int retry;
        private final int retryDelay;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class YggdrasilServiceConfigSerializers implements TypeSerializer<YggdrasilServiceConfig> {
        @Getter
        private static final YggdrasilServiceConfigSerializers instance = new YggdrasilServiceConfigSerializers();

        @Override
        public YggdrasilServiceConfig deserialize(Type type, ConfigurationNode node) throws SerializationException {
            if (!node.hasChild("id")) throw new SerializationException("id not found.");
            int id = node.node("id").getInt();
            if (id < 0 || id > 255) {
                throw new SerializationException(node.node("id"), int.class, String.format("The ID must be in the range 0 to 255, but the id is %d, which exceeds the value range.", id));
            }

            String name = node.node("name").getString("Unnamed");

            HasJoinedConfig hasJoined;
            {
                final ConfigurationNode hasJoinedNode = node.node("hasJoined");
                String url = hasJoinedNode.node("url").getString();
                HttpRequestMethod method = hasJoinedNode.node("method").get(HttpRequestMethod.class, HttpRequestMethod.GET);
                boolean passIp = hasJoinedNode.node("passIp").getBoolean(true);
                String ipContent = hasJoinedNode.node("ipContent").getString("&ip={0}");
                String postContent = hasJoinedNode.node("postContent").getString("{\"username\":\"{0}\", \"serverId\":\"{1}\"}");
                int timeout = hasJoinedNode.node("timeout").getInt(10000);
                int retry = hasJoinedNode.node("retry").getInt(0);
                int retryDelay = hasJoinedNode.node("retryDelay").getInt(0);

                ProxyConfig proxy;
                {
                    proxy = node.node("proxy").get(ProxyConfig.class);
                }

                hasJoined = new HasJoinedConfig(url, method, passIp, ipContent, postContent, timeout, retry, retryDelay, proxy);
            }

            TransformUUID transformUUID = node.node("transformUUID").get(TransformUUID.class, TransformUUID.DEFAULT);
            String nameAllowedRegular = node.node("nameAllowedRegular").getString("");
            boolean whitelist = node.node("whitelist").getBoolean(false);
            boolean refuseRepeatedLogin = node.node("refuseRepeatedLogin").getBoolean(false);
            boolean compulsoryUsername = node.node("compulsoryUsername").getBoolean(false);

            SkinRestorerConfig skinRestorer;
            {
                final ConfigurationNode skinRestorerNode = node.node("skinRestorer");
                SkinRestorerType restorer = skinRestorerNode.node("restorer").get(SkinRestorerType.class, SkinRestorerType.OFF);
                SkinRestorerMethod method = skinRestorerNode.node("method").get(SkinRestorerMethod.class, SkinRestorerMethod.URL);
                int timeout = skinRestorerNode.node("timeout").getInt(10000);
                int retry = skinRestorerNode.node("retry").getInt(2);
                int retryDelay = skinRestorerNode.node("retryDelay").getInt(5000);
                skinRestorer = new SkinRestorerConfig(restorer, method, timeout, retry, retryDelay);
            }

            return new YggdrasilServiceConfig(id, name, hasJoined, transformUUID, nameAllowedRegular, whitelist, refuseRepeatedLogin, compulsoryUsername, skinRestorer);
        }

        @Override
        public void serialize(Type type, @Nullable YggdrasilServiceConfig obj, ConfigurationNode node) {
            throw new UnsupportedOperationException();
        }
    }
}
