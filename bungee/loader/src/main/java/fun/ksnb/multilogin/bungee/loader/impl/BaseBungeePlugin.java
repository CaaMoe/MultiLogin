package fun.ksnb.multilogin.bungee.loader.impl;

import fun.ksnb.multilogin.bungee.loader.main.MultiLoginBungeeLoader;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

public abstract class BaseBungeePlugin {
    private final MultiLoginBungeeLoader loader;

    protected BaseBungeePlugin(MultiLoginBungeeLoader loader) {
        this.loader = loader;
    }

    public abstract void onLoad();

    public abstract void onEnable();

    public abstract void onDisable();

    public PluginDescription getDescription() {
        return loader.getDescription();
    }

    public Plugin getThis() {
        return loader;
    }
}
