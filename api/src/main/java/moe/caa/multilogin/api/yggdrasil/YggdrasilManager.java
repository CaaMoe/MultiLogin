package moe.caa.multilogin.api.yggdrasil;

import java.util.Set;
import java.util.stream.Collectors;

public interface YggdrasilManager {

    /**
     * 通过标识符获取验证服务器数据对象
     *
     * @param path 标识符
     * @return 验证服务器数据对象
     */
    Yggdrasil getYggdrasil(String path);

    /**
     * 获取所有验证服务器数据对象
     *
     * @return 验证服务器数据对象
     */
    Set<Yggdrasil> getAllYggdrasil();

    /**
     * 获取所有被启用的验证服务器数据对象
     *
     * @return 被启用的验证服务器数据对象
     */
    default Set<Yggdrasil> getYggdrasilInEnabled() {
        return getAllYggdrasil().stream().filter(Yggdrasil::isEnabled).collect(Collectors.toSet());
    }
}
