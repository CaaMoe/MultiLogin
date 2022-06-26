package moe.caa.multilogin.api.language;

import moe.caa.multilogin.api.util.Pair;

public interface LanguageAPI {

    /**
     * 通过 节点 和 参数 构建这个可读文本字符串对象
     *
     * @param node 节点
     * @return 可读文本字符串对象
     */
    String getMessage(String node, Pair<?, ?>... pairs);
}
