/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.Verifier
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.database.handler.UserDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

public class Verifier {
    public static final Set<User> CACHE_USER = new HashSet<>();
    public static final Map<UUID, String> CACHE_LOGIN = new HashMap<>();

    public static VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilService yggdrasilService) {
        try {
            User userData = UserDataHandler.getUserEntryByOnlineUuid(onlineUuid);

            boolean updUserEntry = userData != null;

            // 验证服务器不符
            if (updUserEntry) {
                if (!Objects.equals(userData.getYggdrasilService(), yggdrasilService.getPath())) {
                    MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.VERIFICATION_NO_CHAE.getMessage(currentName, onlineUuid.toString(), yggdrasilService.getName(), yggdrasilService.getPath(), userData.getYggdrasilService()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_CHAE.getMessage());
                }
            }

            //名称规范检查
            String reg = ValueUtil.notIsEmpty(yggdrasilService.getNameAllowedRegular()) ?
                    yggdrasilService.getNameAllowedRegular() : ValueUtil.notIsEmpty(MultiCore.nameAllowedRegular) ?
                    "" : MultiCore.nameAllowedRegular;

            if (!reg.isEmpty() && !Pattern.matches(reg, currentName)) {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_PAT_MATCH.getMessage(currentName, onlineUuid.toString(), reg));
                return new VerificationResult(LanguageKeys.VERIFICATION_NO_PAT_MATCH.getMessage());
            }

            //重名检查
            if (!MultiCore.safeId.contains(yggdrasilService.getPath())) {
                List<User> repeatedNameUserEntries = UserDataHandler.getUserEntryByCurrentName(currentName);
                for (User repeatedNameUserEntry : repeatedNameUserEntries) {
                    if (!repeatedNameUserEntry.equals(userData)) {
                        MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_RUSH_NAME.getMessage(currentName, onlineUuid.toString(), repeatedNameUserEntry.getOnlineUuid()));
                        return new VerificationResult(LanguageKeys.VERIFICATION_RUSH_NAME.getMessage());
                    }
                }
            }

            userData = !updUserEntry ? new User(onlineUuid, currentName, yggdrasilService.getConvUuid().getResultUuid(onlineUuid, currentName), yggdrasilService.getPath(), false) : userData;
            userData.setCurrentName(currentName);

            if (!updUserEntry) {
                if (yggdrasilService.getConvRepeat()) {
                    userData.setRedirectUuid(getRepeatUuid(userData, yggdrasilService));
                }
            }

            // 白名单检查
            if (!userData.isWhitelist() && yggdrasilService.getWhitelist()) {
                if (!(CacheWhitelistDataHandler.removeCacheWhitelist(currentName) | CacheWhitelistDataHandler.removeCacheWhitelist(onlineUuid.toString()))) {
                    MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_WHITELIST.getMessage(currentName, onlineUuid.toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_WHITELIST.getMessage());
                }
                userData.setWhitelist(true);
            }

            if (updUserEntry) {
                userData.save();
            } else {
                UserDataHandler.writeNewUserEntry(userData);
            }

            // 重名踢出
            User finalUserData = userData;
            FutureTask<String> task = new FutureTask<>(() -> {
                for (ISender sender : MultiCore.plugin.getPlayer(currentName)) {
                    if (!sender.getPlayerUniqueIdentifier().equals(onlineUuid)) {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_RUSH_NAME_ONL.getMessage());
                    }
                }

                ISender sender = MultiCore.plugin.getPlayer(finalUserData.getRedirectUuid());
                if(sender != null){
                    sender.kickPlayer("爬");
                }
                return null;
            });



            // 等待主线程任务
            MultiCore.plugin.getSchedule().runTask(task);
            task.get();

            CACHE_LOGIN.put(userData.getRedirectUuid(), currentName);
            CACHE_USER.add(userData);
            MultiLogger.log(LoggerLevel.INFO, LanguageKeys.VERIFICATION_ALLOW.getMessage(userData.getOnlineUuid().toString(), userData.getCurrentName(), userData.getRedirectUuid().toString(), yggdrasilService.getName(), yggdrasilService.getPath()));
            return new VerificationResult(userData.getRedirectUuid(), userData);
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.VERIFICATION_ERROR.getMessage());
            return new VerificationResult(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    private static UUID getRepeatUuid(User userData, YggdrasilService yggdrasilService) throws SQLException {
        UUID ret = userData.getRedirectUuid();
        if (UserDataHandler.getUserEntryByRedirectUuid(ret).size() == 0) {
            return ret;
        }
        UUID newUuid = UUID.randomUUID();
        MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_REPEAT_UUID.getMessage(
                userData.getCurrentName(),
                userData.getOnlineUuid(),
                userData.getRedirectUuid().toString(),
                newUuid.toString(),
                yggdrasilService.getName(),
                yggdrasilService.getPath()
        ));
        userData.setRedirectUuid(newUuid);
        return getRepeatUuid(userData, yggdrasilService);
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
        one.removeIf(yggdrasilService -> !yggdrasilService.getEnable());
        List<YggdrasilService> two = new ArrayList<>();
        for (YggdrasilService serviceEntry : YggdrasilServicesHandler.getServices()) {
            if (!serviceEntry.getEnable()) continue;
            if (!one.isEmpty() && one.contains(serviceEntry)) continue;
            two.add(serviceEntry);
        }
        if (!one.isEmpty())
            ret.add(new ArrayList<>(one));
        ret.add(two);
        return ret;
    }
}
