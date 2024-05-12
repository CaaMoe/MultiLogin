package moe.caa.multilogin.api.internal.language;

import moe.caa.multilogin.api.internal.util.Pair;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface LanguageAPI {

    /**
     * 通过 节点 和 参数 构建这个可读文本字符串对象
     *
     * @param node 节点
     * @return 可读文本字符串对象
     */
    String getMessage(String node, Pair<?, ?>... pairs);
}
