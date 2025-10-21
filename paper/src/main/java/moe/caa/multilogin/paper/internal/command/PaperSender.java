package moe.caa.multilogin.paper.internal.command;

import moe.caa.multilogin.common.internal.command.Sender;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class PaperSender<S extends CommandSender> implements Sender {
    protected final S handle;

    public PaperSender(S handle) {
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
}
