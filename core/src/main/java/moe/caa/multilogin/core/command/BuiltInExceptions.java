package moe.caa.multilogin.core.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 自定义消息
 */
public class BuiltInExceptions implements BuiltInExceptionProvider {
    private final Dynamic2CommandExceptionType DOUBLE_TOO_SMALL;
    private final Dynamic2CommandExceptionType DOUBLE_TOO_BIG;
    private final Dynamic2CommandExceptionType FLOAT_TOO_SMALL;
    private final Dynamic2CommandExceptionType FLOAT_TOO_BIG;
    private final Dynamic2CommandExceptionType INTEGER_TOO_SMALL;
    private final Dynamic2CommandExceptionType INTEGER_TOO_BIG;
    private final Dynamic2CommandExceptionType LONG_TOO_SMALL;
    private final Dynamic2CommandExceptionType LONG_TOO_BIG;
    private final DynamicCommandExceptionType LITERAL_INCORRECT;
    private final SimpleCommandExceptionType READER_EXPECTED_START_OF_QUOTE;
    private final SimpleCommandExceptionType READER_EXPECTED_END_OF_QUOTE;
    private final DynamicCommandExceptionType READER_INVALID_ESCAPE;
    private final DynamicCommandExceptionType READER_INVALID_BOOL;
    private final DynamicCommandExceptionType READER_INVALID_INT;
    private final SimpleCommandExceptionType READER_EXPECTED_INT;
    private final DynamicCommandExceptionType READER_INVALID_LONG;
    private final SimpleCommandExceptionType READER_EXPECTED_LONG;
    private final DynamicCommandExceptionType READER_INVALID_DOUBLE;
    private final SimpleCommandExceptionType READER_EXPECTED_DOUBLE;
    private final DynamicCommandExceptionType READER_INVALID_FLOAT;
    private final SimpleCommandExceptionType READER_EXPECTED_FLOAT;
    private final SimpleCommandExceptionType READER_EXPECTED_BOOL;
    private final DynamicCommandExceptionType READER_EXPECTED_SYMBOL;
    private final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND;
    private final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT;
    private final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
    private final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION;

    // custom
    private final SimpleCommandExceptionType REQUIRE_PLAYER;
    private final DynamicCommandExceptionType PLAYER_NOT_ONLINE;

    private final SimpleCommandExceptionType CACHE_NOT_FOUND_SELF;
    private final SimpleCommandExceptionType NO_SELF;

    private final Dynamic2CommandExceptionType CACHE_NOT_FOUND_OTHER;

    public BuiltInExceptions(MultiCore core) {

        DOUBLE_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_double_too_small",
                new Pair<>("found", found),
                new Pair<>("min", min)
        )));
        DOUBLE_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_double_too_big",
                new Pair<>("found", found),
                new Pair<>("max", max)
        )));
        FLOAT_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_float_too_small",
                new Pair<>("found", found),
                new Pair<>("min", min)
        )));
        FLOAT_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_float_too_big",
                new Pair<>("found", found),
                new Pair<>("max", max)
        )));
        INTEGER_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_integer_too_small",
                new Pair<>("found", found),
                new Pair<>("min", min)
        )));
        INTEGER_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_integer_too_big",
                new Pair<>("found", found),
                new Pair<>("max", max)
        )));
        LONG_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_long_too_small",
                new Pair<>("found", found),
                new Pair<>("min", min)
        )));
        LONG_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_long_too_big",
                new Pair<>("found", found),
                new Pair<>("max", max)
        )));
        LITERAL_INCORRECT = new DynamicCommandExceptionType(expected -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_literal_incorrect",
                new Pair<>("expected", expected)
        )));
        READER_EXPECTED_START_OF_QUOTE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_start_of_quote")));
        READER_EXPECTED_END_OF_QUOTE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_end_of_quote")));
        READER_INVALID_ESCAPE = new DynamicCommandExceptionType(character -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_escape",
                new Pair<>("character", character)
        )));
        READER_INVALID_BOOL = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_bool",
                new Pair<>("value", value)
        )));
        READER_INVALID_INT = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_int",
                new Pair<>("value", value)
        )));

        READER_EXPECTED_INT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_int")));
        READER_INVALID_LONG = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_long",
                new Pair<>("value", value)
        )));
        READER_EXPECTED_LONG = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_long")));
        READER_INVALID_DOUBLE = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_double",
                new Pair<>("value", value)
        )));
        READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_double")));
        READER_INVALID_FLOAT = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_float",
                new Pair<>("value", value)
        )));
        READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_float")));
        READER_EXPECTED_BOOL = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_bool")));

        READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType(symbol -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_symbol",
                new Pair<>("symbol", symbol)
        )));
        DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_command")));
        DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_argument")));
        DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_exception_argument_separator")));
        DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType(message -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_parse_exception",
                new Pair<>("command", message)
        )));


        REQUIRE_PLAYER = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_message_require_player")));

        PLAYER_NOT_ONLINE = new DynamicCommandExceptionType(value -> new LiteralMessage(
                core.getLanguageHandler().getMessage("command_message_player_not_online",
                        new Pair<>("name", value)
                )));

        CACHE_NOT_FOUND_SELF = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_message_cache_not_found_self")));
        NO_SELF = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_message_player_no_self")));


        CACHE_NOT_FOUND_OTHER = new Dynamic2CommandExceptionType((uuid, name) -> new LiteralMessage(
                core.getLanguageHandler().getMessage("command_message_cache_not_found_other",
                        new Pair<>("uuid", uuid),
                        new Pair<>("name", name)
                )));
    }

    @Override
    public Dynamic2CommandExceptionType doubleTooLow() {
        return DOUBLE_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType doubleTooHigh() {
        return DOUBLE_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType floatTooLow() {
        return FLOAT_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType floatTooHigh() {
        return FLOAT_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType integerTooLow() {
        return INTEGER_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType integerTooHigh() {
        return INTEGER_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType longTooLow() {
        return LONG_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType longTooHigh() {
        return LONG_TOO_BIG;
    }

    @Override
    public DynamicCommandExceptionType literalIncorrect() {
        return LITERAL_INCORRECT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedStartOfQuote() {
        return READER_EXPECTED_START_OF_QUOTE;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedEndOfQuote() {
        return READER_EXPECTED_END_OF_QUOTE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidEscape() {
        return READER_INVALID_ESCAPE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidBool() {
        return READER_INVALID_BOOL;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidInt() {
        return READER_INVALID_INT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedInt() {
        return READER_EXPECTED_INT;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidLong() {
        return READER_INVALID_LONG;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedLong() {
        return READER_EXPECTED_LONG;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidDouble() {
        return READER_INVALID_DOUBLE;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedDouble() {
        return READER_EXPECTED_DOUBLE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidFloat() {
        return READER_INVALID_FLOAT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedFloat() {
        return READER_EXPECTED_FLOAT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedBool() {
        return READER_EXPECTED_BOOL;
    }

    @Override
    public DynamicCommandExceptionType readerExpectedSymbol() {
        return READER_EXPECTED_SYMBOL;
    }

    @Override
    public SimpleCommandExceptionType dispatcherUnknownCommand() {
        return DISPATCHER_UNKNOWN_COMMAND;
    }

    @Override
    public SimpleCommandExceptionType dispatcherUnknownArgument() {
        return DISPATCHER_UNKNOWN_ARGUMENT;
    }

    @Override
    public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
        return DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
    }

    @Override
    public DynamicCommandExceptionType dispatcherParseException() {
        return DISPATCHER_PARSE_EXCEPTION;
    }

    public SimpleCommandExceptionType requirePlayer() {
        return REQUIRE_PLAYER;
    }

    public DynamicCommandExceptionType playerNotOnline() {
        return PLAYER_NOT_ONLINE;
    }

    public SimpleCommandExceptionType noSelf() {
        return NO_SELF;
    }

    public SimpleCommandExceptionType cacheNotFoundSelf() {
        return CACHE_NOT_FOUND_SELF;
    }

    public Dynamic2CommandExceptionType cacheNotFoundOther() {
        return CACHE_NOT_FOUND_OTHER;
    }
}