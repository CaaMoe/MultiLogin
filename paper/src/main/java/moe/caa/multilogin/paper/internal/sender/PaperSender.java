package moe.caa.multilogin.paper.internal.sender;

import moe.caa.multilogin.common.internal.data.Sender;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public sealed class PaperSender<S extends CommandSender> implements Sender permits PaperOnlinePlayer {
    protected final S handle;

    PaperSender(S handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(Component component) {
        handle.sendMessage(component);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaperSender<?> that = (PaperSender<?>) o;
        return Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(handle);
    }

    public static <S extends CommandSender> Sender wrapSender(S sender){
        return switch (sender) {
            case Player p -> wrapSender(p);
            default -> new PaperSender<>(sender);
        };
    }
    public static PaperOnlinePlayer wrapSender(Player player){
        return new PaperOnlinePlayer(player);
    }
}
