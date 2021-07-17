package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.database.handler.UserDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;

public class Verifier {

    public static VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilService yggdrasilService) {
        try {
            User userData = UserDataHandler.getUserEntryByOnlineUuid(onlineUuid);

            boolean updUserEntry = userData != null;

            // 验证服务器不符
            if (updUserEntry) {
                if (!Objects.equals(userData.yggdrasilService, yggdrasilService.path)) {
                    MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.VERIFICATION_NO_CHAE.getMessage(currentName, onlineUuid.toString(), yggdrasilService.name, yggdrasilService.path, userData.yggdrasilService));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_CHAE.getMessage());
                }
            }

            //重名检查
            if (!MultiCore.safeId.contains(yggdrasilService.path)) {
                List<User> repeatedNameUserEntries = UserDataHandler.getUserEntryByCurrentName(currentName);
                for (User repeatedNameUserEntry : repeatedNameUserEntries) {
                    if (!repeatedNameUserEntry.equals(userData)) {
                        MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_RUSH_NAME.getMessage(currentName, onlineUuid.toString(), repeatedNameUserEntry.onlineUuid));
                        return new VerificationResult(LanguageKeys.VERIFICATION_RUSH_NAME.getMessage());
                    }
                }
            }

            userData = !updUserEntry ? new User(onlineUuid, currentName, yggdrasilService.convUuid.getResultUuid(onlineUuid, currentName), yggdrasilService.path, false) : userData;
            userData.currentName = currentName;

            // 白名单检查
            if (!userData.whitelist && yggdrasilService.whitelist) {
                if (!(CacheWhitelistDataHandler.removeCacheWhitelist(currentName) | CacheWhitelistDataHandler.removeCacheWhitelist(onlineUuid.toString()))) {
                    MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_WHITELIST.getMessage(currentName, onlineUuid.toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_WHITELIST.getMessage());
                }
                userData.whitelist = true;
            }

            if (updUserEntry) {
                UserDataHandler.updateUserEntry(userData);
            } else {
                UserDataHandler.writeNewUserEntry(userData);
            }

            // 重名踢出
            FutureTask<String> task = new FutureTask<>(() -> {
                for (ISender sender : MultiCore.plugin.getPlayer(currentName)) {
                    if (!sender.getPlayerUniqueIdentifier().equals(onlineUuid)) {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_RUSH_NAME_ONL.getMessage());
                    }
                }


                return null;
            });

            // 等待主线程任务
            MultiCore.plugin.getSchedule().runTask(task);
            task.get();

            MultiLogger.log(LoggerLevel.INFO, LanguageKeys.VERIFICATION_ALLOW.getMessage(userData.redirectUuid.toString(), userData.currentName, userData.onlineUuid.toString(), yggdrasilService.name, yggdrasilService.path));
            return new VerificationResult(userData.redirectUuid, userData);
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.VERIFICATION_ERROR.getMessage());
            return new VerificationResult(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    /**
     * 按照玩家名字分批次排序 Yggdrasil 验证服务器
     *
     * @param name name
     * @return 验证服务器排序的结果
     */
    public static List<List<YggdrasilService>> getVeriOrder(String name) throws SQLException {
        List<List<YggdrasilService>> ret = new ArrayList<>();
        Set<YggdrasilService> one = UserDataHandler.getYggdrasilServiceByCurrentName(name);
        one.removeIf(yggdrasilService -> !yggdrasilService.enable);
        List<YggdrasilService> two = new ArrayList<>();
        for (YggdrasilService serviceEntry : YggdrasilServicesHandler.getServices()) {
            if (!serviceEntry.enable) continue;
            if (!one.isEmpty() && one.contains(serviceEntry)) continue;
            two.add(serviceEntry);
        }
        if (!one.isEmpty())
            ret.add(new ArrayList<>(one));
        ret.add(two);
        return ret;
    }
}
