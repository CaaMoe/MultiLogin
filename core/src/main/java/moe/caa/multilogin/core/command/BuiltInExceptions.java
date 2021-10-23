package moe.caa.multilogin.core.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

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

    public BuiltInExceptions(MultiCore core) {

        DOUBLE_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_double_too_small", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("min").content(min).build()
        ))));
        DOUBLE_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_double_too_big", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("max").content(max).build()
        ))));
        FLOAT_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_float_too_small", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("min").content(min).build()
        ))));
        FLOAT_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_float_too_big", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("max").content(max).build()
        ))));
        INTEGER_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_integer_too_small", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("min").content(min).build()
        ))));
        INTEGER_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_integer_too_big", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("max").content(max).build()
        ))));
        LONG_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_long_too_small", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("min").content(min).build()
        ))));
        LONG_TOO_BIG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_long_too_big", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("found").content(found).build(),
                FormatContent.FormatEntry.builder().name("max").content(max).build()
        ))));
        LITERAL_INCORRECT = new DynamicCommandExceptionType(expected -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_literal_incorrect", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("expected").content(expected).build()
        ))));
        READER_EXPECTED_START_OF_QUOTE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_start_of_quote", FormatContent.empty())));
        READER_EXPECTED_END_OF_QUOTE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_end_of_quote", FormatContent.empty())));
        READER_INVALID_ESCAPE = new DynamicCommandExceptionType(character -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_escape", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("character").content(character).build()
        ))));
        READER_INVALID_BOOL = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_bool", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("value").content(value).build()
        ))));
        READER_INVALID_INT = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_int", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("value").content(value).build()
        ))));

        READER_EXPECTED_INT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_int", FormatContent.empty())));
        READER_INVALID_LONG = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_long", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("value").content(value).build()
        ))));
        READER_EXPECTED_LONG = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_long", FormatContent.empty())));
        READER_INVALID_DOUBLE = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_double", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("value").content(value).build()
        ))));
        READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_double", FormatContent.empty())));
        READER_INVALID_FLOAT = new DynamicCommandExceptionType(value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_float", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("value").content(value).build()
        ))));
        READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_float", FormatContent.empty())));
        READER_EXPECTED_BOOL = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_bool", FormatContent.empty())));

        READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType(symbol -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_symbol", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("symbol").content(symbol).build()
        ))));
        DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_command", FormatContent.empty())));
        DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_argument", FormatContent.empty())));
        DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_exception_argument_separator", FormatContent.empty())));
        DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType(message -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_parse_exception", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("message").content(message).build()
        ))));
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
}
