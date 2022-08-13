package fun.ksnb.multilogin.velocity.main.v5t.redirect;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.crypto.SignaturePair;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerCommand;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap;
import io.netty.buffer.ByteBuf;
import moe.caa.multilogin.core.logger.LoggerLevel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class MultiPlayerCommand extends PlayerCommand {
    private static MethodHandle previousMessagesFieldSetter;
    private static MethodHandle lastMessageFieldSetter;


    public static void init() throws NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Field previousMessagesField = PlayerCommand.class.getDeclaredField("previousMessages");
        previousMessagesField.setAccessible(true);
        MultiPlayerCommand.previousMessagesFieldSetter = lookup.unreflectSetter(previousMessagesField);

        Field lastMessageField = PlayerCommand.class.getDeclaredField("lastMessage");
        lastMessageField.setAccessible(true);
        MultiPlayerCommand.lastMessageFieldSetter = lookup.unreflectSetter(lastMessageField);


    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        removeSignature();
        super.encode(buf, direction, protocolVersion);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        removeSignature();
        return super.handle(handler);
    }

    private void removeSignature() {
        try {
            previousMessagesFieldSetter.invoke(this, new SignaturePair[0]);
            lastMessageFieldSetter.invoke(this, null);
        } catch (Throwable throwable) {
            MultiLoginVelocityPluginBootstrap.getInstance().getCore().getLogger().log(LoggerLevel.ERROR,
                    "An exception was encountered while clearing the command signature.", throwable);
        }
    }
}
