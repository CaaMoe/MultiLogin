package moe.caa.multilogin.core.util;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 代表占位详情
 */
@Getter
public class FormatContent {
    private final List<FormatEntry> formatEntries;

    public FormatContent(FormatEntry... entries) {
        formatEntries = Arrays.asList(entries);
    }

    /**
     * 代表占位节
     */
    @Data
    @Builder()
    public static class FormatEntry {
        private final String name;
        private final Object content;
    }
}
