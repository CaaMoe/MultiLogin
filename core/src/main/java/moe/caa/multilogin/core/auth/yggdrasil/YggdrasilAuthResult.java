package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

/**
 * Yggdrasil HasJoined 验证结果
 */
@AllArgsConstructor
public class YggdrasilAuthResult {
    public final YggdrasilAuthReasonEnum err;
    public final HasJoinedResponse result;
    public final YggdrasilService service;
    public final Throwable throwable;

    /**
     * 身份验证是否成功
     *
     * @return 验证是否成功
     */
    public boolean isSuccess() {
        return result != null && result.getId() != null && err == YggdrasilAuthReasonEnum.RETURN && throwable == null;
    }
}
