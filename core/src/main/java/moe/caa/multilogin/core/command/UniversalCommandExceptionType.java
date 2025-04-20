package moe.caa.multilogin.core.command;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UniversalCommandExceptionType implements CommandExceptionType {
    private static final UniversalCommandExceptionType instance = new UniversalCommandExceptionType();

    public static CommandSyntaxException create(Message message){
        return new CommandSyntaxException(instance, message);
    }

    public static CommandSyntaxException create(String message){
        return create(new LiteralMessage(message));
    }

    public static CommandSyntaxException create(Message message, ImmutableStringReader reader) {
        return new CommandSyntaxException(instance, message, reader.getString(), reader.getCursor());
    }

    public static CommandSyntaxException create(String message, ImmutableStringReader reader) {
        return new CommandSyntaxException(instance, new LiteralMessage(message), reader.getString(), reader.getCursor());
    }
}
