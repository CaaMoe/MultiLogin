package moe.caa.multilogin.common.internal.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

public record EditableMiniMessage(String originalMiniMessageStr) {
    public EditableMiniMessage replace(String target, String replacement) {
        return new EditableMiniMessage(originalMiniMessageStr.replace(target, replacement));
    }

    public EditableMiniMessage replace(Map<String, String> placeholders) {
        String string = originalMiniMessageStr;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return new EditableMiniMessage(string);
    }

    public Component build() {
        return MiniMessage.miniMessage().deserialize(originalMiniMessageStr);
    }
}
