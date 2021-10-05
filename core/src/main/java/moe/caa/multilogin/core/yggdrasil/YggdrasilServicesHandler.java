package moe.caa.multilogin.core.yggdrasil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.YamlReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 代表 Yggdrasil 账户验证服务器数据对象管理程序
 */
@NoArgsConstructor
@Getter
public class YggdrasilServicesHandler {
    private final Set<YggdrasilService> allServices = Collections.synchronizedSet(new HashSet<>());
    private final Set<YggdrasilService> enabledServices = Collections.synchronizedSet(new HashSet<>());

    /**
     * 读取配置
     *
     * @param config 配置文件
     */
    public synchronized void init(YamlReader config) {
        reload(config);
    }

    /**
     * 重新加载配置
     *
     * @param config 配置
     */
    public synchronized void reload(YamlReader config) {
        allServices.clear();
        enabledServices.clear();
        for (var key : config.getKeys()) {
            var section = config.get(key, YamlReader.class);
            if (section == null) continue;
            try {
                var service = YggdrasilService.getYggdrasilServiceFromMap(key, section);
                allServices.add(service);
                if (service.isEnable()) enabledServices.add(service);
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Add " + service);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("添加 Yggdrasil 账户验证服务器 %s(%s)%s。",
                        service.getName(), service.getPath(), (service.isEnable() ? "" : "，但未启用它")));

            } catch (Exception e) {
                MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("无法读取配置文件节点 '%s' 中设置的 Yggdrasil 账户验证服务器", key), e);
            }
        }
        if (enabledServices.isEmpty())
            MultiLogger.getLogger().log(LoggerLevel.WARN, "尚未启用任何 Yggdrasil 账户验证服务器，将拒绝所有玩家登入游戏.");
    }

    /**
     * 通过标识符获取被启用的验证服务器数据对象
     *
     * @param path 标识符
     * @return 被启用的验证服务器数据对象
     */
    public YggdrasilService getYggdrasilServiceInEnabled(String path) {
        for (YggdrasilService service : enabledServices) {
            if (service.getPath().equals(path)) {
                return service;
            }
        }
        return null;
    }

    /**
     * 通过标识符获取验证服务器数据对象
     *
     * @param path 标识符
     * @return 验证服务器数据对象
     */
    public YggdrasilService getYggdrasilService(String path) {
        for (YggdrasilService service : allServices) {
            if (service.getPath().equals(path)) {
                return service;
            }
        }
        return null;
    }
}
