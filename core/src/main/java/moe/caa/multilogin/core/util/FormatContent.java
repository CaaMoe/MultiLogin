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

    private FormatContent(FormatEntry... entries) {
        formatEntries = Arrays.asList(entries);
    }

    /**
     * 构建空占位内容
     *
     * @return 空占位内容
     */
    public static FormatContent empty() {
        return new FormatContent();
    }

    /**
     * 创建一个占位内容
     *
     * @param entries 占位聚合
     * @return 占位内容
     */
    public static FormatContent createContent(FormatEntry... entries) {
        return new FormatContent(entries);
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
