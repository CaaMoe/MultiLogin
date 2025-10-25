package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.Map;

public class ProfileLimitConfig extends Configuration {
    public final ConfigurationValue<Integer> defaultMaxSlotCount = integerOpt(NodePath.path("default-max-slot-count"), 2);
    public final ConfigurationValue<Map<String, Integer>> permissionMaxSlotCounts = raw(NodePath.path("permission-max-slot-counts"), configurationNode -> {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Integer> map = (Map<String, Integer>) configurationNode.get(Object.class);
            if (map == null) map = Collections.emptyMap();
            return map;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    });

    public final ConfigurationValue<String> profileNameRegularExpressionRestriction = stringOpt(NodePath.path("profile-name-regular-expression-restriction"), "^[a-zA-Z0-9_]{1,16}$");
}
