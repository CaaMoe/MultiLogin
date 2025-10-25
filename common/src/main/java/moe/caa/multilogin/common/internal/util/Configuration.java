package moe.caa.multilogin.common.internal.util;

import moe.caa.multilogin.common.internal.main.MultiCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Configuration {
    private final List<ParseableConfigurationValue<?>> configurationValues = new ArrayList<>();

    protected <E extends Enum<E>> ConfigurationValue<E> enumConstant(NodePath path, Class<E> enumClass) {
        return raw(path, node -> {
            String value = node.getString();
            if (value == null) {
                return null;
            }

            for (E constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(StringUtil.kebabCaseToUnderscoreUpperCase(value))) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for enum " + enumClass.getName() + " at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")));

        });
    }

    protected <E extends Enum<E>> ConfigurationValue<E> enumConstantOpt(NodePath path, Class<E> enumClass, E defaultValue) {
        return raw(path, node -> {
            String value = node.getString(defaultValue.name());
            if (value == null) {
                return null;
            }

            for (E constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(StringUtil.kebabCaseToUnderscoreUpperCase(value))) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for enum " + enumClass.getName() + " at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")));

        });
    }

    protected ConfigurationValue<PublicKey> rsaPublicKey(NodePath path) {
        return raw(path, node -> {
            String string = node.getString();
            if (string == null) return null;
            Path resolve = MultiCore.instance.platform.getPlatformConfigPath().resolve(string).normalize();

            try {
                return RSAUtil.loadPublicKey(resolve);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid public key file " + resolve.toFile().getAbsolutePath());
            }
        });
    }

    protected ConfigurationValue<PrivateKey> rsaPrivateKey(NodePath path) {
        return raw(path, node -> {
            String string = node.getString();
            if (string == null) return null;
            Path resolve = MultiCore.instance.platform.getPlatformConfigPath().resolve(string).normalize();

            try {
                return RSAUtil.loadPrivateKey(resolve);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid private key file " + resolve.toFile().getAbsolutePath());
            }
        });
    }

    protected ConfigurationValue<Component> miniMsg(NodePath path) {
        return raw(path, node -> {
            String string = node.getString();
            if (string == null) return null;
            return MiniMessage.miniMessage().deserialize(string);
        });
    }

    protected ConfigurationValue<Component> miniMsgOpt(NodePath path, Component defaultValue) {
        return raw(path, node -> {
            String string = node.getString();
            if (string == null) return defaultValue;
            return MiniMessage.miniMessage().deserialize(string);
        });
    }

    protected ConfigurationValue<Boolean> bool(NodePath path) {
        return raw(path, ConfigurationNode::getBoolean);
    }

    protected ConfigurationValue<Boolean> boolOpt(NodePath path, boolean defaultValue) {
        return raw(path, node -> node.getBoolean(defaultValue));
    }

    protected ConfigurationValue<Integer> integer(NodePath path) {
        return raw(path, ConfigurationNode::getInt);
    }

    protected ConfigurationValue<Integer> integerOpt(NodePath path, int defaultValue) {
        return raw(path, node -> node.getInt(defaultValue));
    }

    protected ConfigurationValue<String> string(NodePath path) {
        return raw(path, ConfigurationNode::getString);
    }

    protected ConfigurationValue<String> stringOpt(NodePath path, String defaultValue) {
        return raw(path, node -> node.getString(defaultValue));
    }

    protected <T extends Configuration> ConfigurationValue<T> sub(NodePath path, T configurationInstance) {
        return new ConfigurationSubConfiguration<>(path, configurationInstance);
    }

    protected <T> ConfigurationValue<T> raw(NodePath path, Function<ConfigurationNode, T> mapValue) {
        return new ConfigurationSpecifiedValue<>(path, mapValue, () -> {
            throw new IllegalArgumentException(
                    Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")) + " is a required value, but it is empty."
            );
        });
    }

    public void loadFrom(ConfigurationNode node, ConfigurationNode defaultNode) {
        for (var configurationValue : configurationValues) {
            switch (configurationValue) {
                case ConfigurationSpecifiedValue<?> csv ->
                        configurationValue.parse(node.node(csv.path), defaultNode.node(csv.path));
                case ConfigurationSubConfiguration<?> csc ->
                        configurationValue.parse(node.node(csc.path), defaultNode.node(csc.path));
            }
        }
    }

    public void loadFrom(ConfigurationNode node) {
        for (var configurationValue : configurationValues) {
            switch (configurationValue) {
                case ConfigurationSpecifiedValue<?> csv ->
                        configurationValue.parse(node.node(csv.path), node.node(csv.path));
                case ConfigurationSubConfiguration<?> csc ->
                        configurationValue.parse(node.node(csc.path), node.node(csc.path));
            }
        }
    }

    public sealed interface ConfigurationValue<T> {
        T get();
    }

    private sealed interface ParseableConfigurationValue<T> extends ConfigurationValue<T> {
        void parse(ConfigurationNode node, ConfigurationNode defaultNode);
    }

    private final class ConfigurationSubConfiguration<T extends Configuration> implements ParseableConfigurationValue<T> {
        private final NodePath path;
        private final T configurationInstance;

        public ConfigurationSubConfiguration(NodePath path, T configurationInstance) {
            this.path = path;
            this.configurationInstance = configurationInstance;

            configurationValues.add(this);
        }

        @Override
        public T get() {
            return configurationInstance;
        }

        @Override
        public void parse(ConfigurationNode node, ConfigurationNode defaultNode) {
            configurationInstance.loadFrom(node, defaultNode);
        }
    }

    private final class ConfigurationSpecifiedValue<T> implements ParseableConfigurationValue<T> {
        private final NodePath path;
        private final Function<ConfigurationNode, T> mapValue;
        private final Supplier<T> defaultProvider;
        private T parsedValue;

        public ConfigurationSpecifiedValue(
                NodePath path,
                Function<ConfigurationNode, T> mapValue,
                Supplier<T> defaultProvider
        ) {
            this.path = path;
            this.mapValue = mapValue;
            this.defaultProvider = defaultProvider;

            configurationValues.add(this);
        }

        @Override
        public void parse(ConfigurationNode node, ConfigurationNode defaultNode) {
            parsedValue = Objects.requireNonNullElseGet(mapValue.apply(node),
                    () -> Objects.requireNonNullElseGet(mapValue.apply(defaultNode), defaultProvider)
            );
        }

        @Override
        public T get() {
            if (parsedValue == null) {
                throw new IllegalStateException("Configuration value at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")) + " has not been parsed yet.");
            }
            return parsedValue;
        }
    }
}
