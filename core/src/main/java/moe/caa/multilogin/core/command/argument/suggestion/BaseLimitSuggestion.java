package moe.caa.multilogin.core.command.argument.suggestion;

import moe.caa.multilogin.core.command.CommandHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 限制补全内容数量的补全器
 */
public abstract class BaseLimitSuggestion<O> implements Suggestion {
    private final Function<O, String> objMap;

    protected BaseLimitSuggestion(Function<O, String> objMap) {
        this.objMap = objMap;
    }

    @Override
    public final Set<String> suggestion(String remaining) {
        Set<String> strings = new HashSet<>();
        int limit = CommandHandler.getCore().getPluginConfig().getCommandSuggestionLimit();
        for (O o : getSource()) {
            if(!hasRemain(o, remaining)) continue;
            if (--limit > 0){
                strings.add(objMap.apply(o));
            } else {
                break;
            }
        }
        return strings;
    }

    public abstract Collection<O> getSource();

    public boolean hasRemain(O o, String remaining){
        return objMap.apply(o).toLowerCase().startsWith(remaining.toLowerCase());
    }
}
