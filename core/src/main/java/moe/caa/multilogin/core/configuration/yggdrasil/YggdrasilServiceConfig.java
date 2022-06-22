package moe.caa.multilogin.core.configuration.yggdrasil;

import lombok.*;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.skinrestorer.SkinRestorerConfig;
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
    private final InitUUID initUUID;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final boolean compulsoryUsername;
    private final SkinRestorerConfig skinRestorer;

    // TODO: 2022/6/2
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class YggdrasilServiceConfigSerializers implements TypeSerializer<YggdrasilServiceConfig> {
        @Getter
        private static final YggdrasilServiceConfigSerializers instance = new YggdrasilServiceConfigSerializers();

        @Override
        public YggdrasilServiceConfig deserialize(Type t, ConfigurationNode node) throws SerializationException {
            int id = node.node("id").getInt();
            String name = node.node("name").getString("Unnamed");
            HasJoinedConfig hasJoined = node.node("hasJoined").get(HasJoinedConfig.class);
            InitUUID initUUID = node.node("initUUID").get(InitUUID.class, InitUUID.DEFAULT);
            String nameAllowedRegular = node.node("nameAllowedRegular").getString("");
            boolean whitelist = node.node("whitelist").getBoolean(false);
            boolean refuseRepeatedLogin = node.node("refuseRepeatedLogin").getBoolean(false);
            boolean compulsoryUsername = node.node("compulsoryUsername").getBoolean(false);
            SkinRestorerConfig skinRestorer = node.node("skinRestorer").get(SkinRestorerConfig.class);
            return new YggdrasilServiceConfig(id, name, hasJoined, initUUID, nameAllowedRegular, whitelist, refuseRepeatedLogin, compulsoryUsername, skinRestorer);
        }

        @Override
        public void serialize(Type type, @Nullable YggdrasilServiceConfig obj, ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException();
        }
    }

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

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class HasJoinedConfigSerializers implements TypeSerializer<HasJoinedConfig> {
            @Getter
            private static final HasJoinedConfigSerializers instance = new HasJoinedConfigSerializers();

            @Override
            public HasJoinedConfig deserialize(Type t, ConfigurationNode node) throws SerializationException {
                String url = node.node("url").getString();
                HttpRequestMethod method = node.node("method").get(HttpRequestMethod.class, HttpRequestMethod.GET);
                boolean passIp = node.node("passIp").getBoolean(true);
                String ipContent = node.node("ipContent").getString("&ip={0}");
                String postContent = node.node("postContent").getString("{\"username\":\"{0}\", \"serverId\":\"{1}\"}");
                int timeout = node.node("timeout").getInt(10000);
                int retry = node.node("retry").getInt(0);
                int retryDelay = node.node("retryDelay").getInt(0);
                ProxyConfig proxy = node.node("proxy").get(ProxyConfig.class);
                return new HasJoinedConfig(url, method, passIp, ipContent, postContent, timeout, retry, retryDelay, proxy);
            }

            @Override
            public void serialize(Type type, @Nullable HasJoinedConfig obj, ConfigurationNode node) throws SerializationException {
                throw new UnsupportedOperationException();
            }
        }
    }
}
