package moe.caa.multilogin.common.internal.util;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

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
                if (constant.name().equalsIgnoreCase(value)) {
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
                if (constant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for enum " + enumClass.getName() + " at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")));

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

    public void loadFrom(ConfigurationNode node) {
        for (var configurationValue : configurationValues) {
            switch (configurationValue) {
                case ConfigurationSpecifiedValue<?> csv -> configurationValue.parse(node.node(csv.path));
                case ConfigurationSubConfiguration<?> csc -> configurationValue.parse(node.node(csc.path));
            }
        }
    }

    public sealed interface ConfigurationValue<T> {
        T get();
    }

    private sealed interface ParseableConfigurationValue<T> extends ConfigurationValue<T> {
        void parse(ConfigurationNode node);
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
        public void parse(ConfigurationNode node) {
            configurationInstance.loadFrom(node);
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
        public void parse(ConfigurationNode node) {
            parsedValue = Objects.requireNonNullElseGet(mapValue.apply(node), defaultProvider);
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
