package moe.caa.multilogin.core.yggdrasil;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.YamlConfig;

import java.util.ArrayList;

public class YggdrasilServicesHandler {
    private static final ArrayList<YggdrasilService> SERVICES = new ArrayList<>();

    public static void init(){
        reload();
    }

    public static void reload(){
        SERVICES.clear();
        YamlConfig services = MultiCore.config.get("services", YamlConfig.class);
        if(services != null){
            for(String path : services.getKeys()){
                YamlConfig section = services.get(path, YamlConfig.class);
                if(section == null) continue;
                try {
                    YggdrasilService service = YggdrasilService.fromYamlConfig(path, section);
                    SERVICES.add(service);
                    if (service.enable) {
                        MultiLogger.log(LoggerLevel.INFO, LanguageKeys.APPLY_YGGDRASIL.getMessage(service.name, service.path));
                    } else {
                        MultiLogger.log(LoggerLevel.INFO, LanguageKeys.APPLY_YGGDRASIL_NO_ENABLE.getMessage(service.name, service.path));
                    }
                } catch (Exception e){
                    MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.YGGDRASIL_CONFIGURATION_ERROR.getMessage(path, e.getMessage()));
                }
            }
        }
        if (SERVICES.stream().noneMatch(yggdrasilService -> yggdrasilService.enable)){
            MultiLogger.log(LoggerLevel.WARN, LanguageKeys.SERVICES_NOTHING.getMessage());
        }
    }

    public static YggdrasilService getService(String path){
        for (YggdrasilService service : SERVICES) {
            if (service.path.equals(path)){
                return service;
            }
        }
        return null;
    }
}
