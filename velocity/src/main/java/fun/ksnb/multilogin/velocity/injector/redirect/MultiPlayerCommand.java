package fun.ksnb.multilogin.velocity.injector.redirect;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerCommand;

public class MultiPlayerCommand extends PlayerCommand {

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return super.handle(handler);
    }
}
