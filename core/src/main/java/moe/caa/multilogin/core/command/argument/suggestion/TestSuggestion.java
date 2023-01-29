package moe.caa.multilogin.core.command.argument.suggestion;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestSuggestion extends BaseLimitSuggestion<String>{
    protected TestSuggestion() {
        super(s -> s);
    }

    @Override
    public Collection<String> getSource() {
        Set<String> stringSet = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            stringSet.add(String.valueOf(i));
        }
        return stringSet;
    }
}
