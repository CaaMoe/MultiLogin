package moe.caa.multilogin.core.yggdrasil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.YamlConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 代表 Yggdrasil 账户验证服务器数据对象管理程序
 */
@NoArgsConstructor
@Getter
public class YggdrasilServicesHandler {
    private final Set<YggdrasilService> yggdrasilServices = Collections.synchronizedSet(new HashSet<>());

    /**
     * 读取配置
     *
     * @param config 配置文件
     */
    public void init(YamlConfig config) {
        yggdrasilServices.clear();
        for (var key : config.getKeys()) {
            var section = config.get(key, YamlConfig.class);
            if (section == null) continue;
            try {
                var service = YggdrasilService.getYggdrasilServiceFromMap(key, section);
                yggdrasilServices.add(service);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("添加 Yggdrasil 账户验证服务器 %s(%s)%s。",
                        service.getNameString(), service.getPathString(), (service.isEnable() ? "" : "，但未启用它")));
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, service.toString());
            } catch (Exception e) {
                MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("无法读取配置文件节点 '%s' 中的 Yggdrasil 账户验证服务器", key), e);
            }
        }
    }

    public YggdrasilService getYggdrasilService(String path) {
        for (YggdrasilService service : yggdrasilServices) {
            if (service.getPathString().equals(path)) {
                return service;
            }
        }
        return null;
    }
}
