package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfiguration = stringOpt(NodePath.path("database-configuration"), "hikari.properties");
}
