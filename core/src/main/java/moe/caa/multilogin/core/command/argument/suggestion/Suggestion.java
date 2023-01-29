package moe.caa.multilogin.core.command.argument.suggestion;

import java.util.Set;
import java.util.function.Function;

/**
 * 基本的补全器
 */
public interface Suggestion {
    Set<String> suggestion(String remaining);
}
