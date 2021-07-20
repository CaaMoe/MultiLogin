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
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

public class Verifier {
    public final Set<User> CACHE_USER = new HashSet<>();
    public final Map<UUID, String> CACHE_LOGIN = new HashMap<>();
    public final MultiCore core;

    public Verifier(MultiCore multicore) {
        core = multicore;
    }

    public VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilService yggdrasilService) {
        try {
            User userData = core.getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(onlineUuid);

            boolean updUserEntry = userData != null;

            // 验证服务器不符
            if (updUserEntry) {
                if (!Objects.equals(userData.getYggdrasilService(), yggdrasilService.getPath())) {
                    core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.VERIFICATION_NO_CHAE.getMessage(core, currentName, onlineUuid.toString(), yggdrasilService.getName(), yggdrasilService.getPath(), userData.getYggdrasilService()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_CHAE.getMessage(core));
                }
            }

            //名称规范检查
            String reg = ValueUtil.notIsEmpty(yggdrasilService.getNameAllowedRegular()) ?
                    yggdrasilService.getNameAllowedRegular() : ValueUtil.notIsEmpty(core.nameAllowedRegular) ?
                    "" : core.nameAllowedRegular;

            if (!reg.isEmpty() && !Pattern.matches(reg, currentName)) {
                core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_PAT_MATCH.getMessage(core, currentName, onlineUuid.toString(), reg));
                return new VerificationResult(LanguageKeys.VERIFICATION_NO_PAT_MATCH.getMessage(core));
            }

            //重名检查
            if (!core.safeId.contains(yggdrasilService.getPath())) {
                List<User> repeatedNameUserEntries = core.getSqlManager().getUserDataHandler().getUserEntryByCurrentName(currentName);
                for (User repeatedNameUserEntry : repeatedNameUserEntries) {
                    if (!repeatedNameUserEntry.equals(userData)) {
                        core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_RUSH_NAME.getMessage(core, currentName, onlineUuid.toString(), repeatedNameUserEntry.getOnlineUuid()));
                        return new VerificationResult(LanguageKeys.VERIFICATION_RUSH_NAME.getMessage(core));
                    }
                }
            }

            userData = !updUserEntry ? new User(onlineUuid, currentName, yggdrasilService.getConvUuid().getResultUuid(onlineUuid, currentName), yggdrasilService.getPath(), false, core.getYggdrasilServicesHandler()) : userData;
            userData.setCurrentName(currentName);

            if (!updUserEntry) {
                if (yggdrasilService.isConvRepeat()) {
                    userData.setRedirectUuid(getRepeatUuid(userData, yggdrasilService));
                }
            }

            // 白名单检查
            if (!userData.isWhitelist() && yggdrasilService.isWhitelist()) {
                if (!(core.getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(currentName) | core.getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(onlineUuid.toString()))) {
                    core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_WHITELIST.getMessage(core, currentName, onlineUuid.toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_WHITELIST.getMessage(core));
                }
                userData.setWhitelist(true);
            }

            if (updUserEntry) {
                core.getSqlManager().getUserDataHandler().updateUserEntry(userData);
            } else {
                core.getSqlManager().getUserDataHandler().writeNewUserEntry(userData);
            }

            // 重名踢出，返回重复 Redirect uuid 的 Sender 对象
            User finalUserData = userData;
            FutureTask<ISender> task = new FutureTask<>(() -> {
                for (ISender sender : core.plugin.getPlayer(currentName)) {
                    if (!sender.getPlayerUniqueIdentifier().equals(onlineUuid)) {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_RUSH_NAME_ONL.getMessage(core));
                    }
                }

                return core.plugin.getPlayer(finalUserData.getRedirectUuid());
            });


            // 等待主线程任务
            core.plugin.getSchedule().runTask(task);
            ISender sender = task.get();

            // 重复登入验证
            if (sender != null) {
                if (yggdrasilService.isRefuseRepeatedLogin()) {
                    core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_REPEATED_LOGIN.getMessage(core, userData.getCurrentName(), userData.getOnlineUuid().toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_ANOTHER_LOGIN_SQUEEZE.getMessage(core));
                } else {
                    FutureTask<ISender> kick = new FutureTask<>(() -> {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_SELF_LOGIN_SQUEEZE.getMessage(core));
                        return null;
                    });
                    core.plugin.getSchedule().runTask(kick);
                    kick.get();
                }
            }

            CACHE_LOGIN.put(userData.getRedirectUuid(), currentName);
            CACHE_USER.add(userData);
            core.getLogger().log(LoggerLevel.INFO, LanguageKeys.VERIFICATION_ALLOW.getMessage(core, userData.getOnlineUuid().toString(), userData.getCurrentName(), userData.getRedirectUuid().toString(), yggdrasilService.getName(), yggdrasilService.getPath()));
            return new VerificationResult(userData.getRedirectUuid(), userData);
        } catch (Exception e) {
            core.getLogger().log(LoggerLevel.ERROR, e);
            core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.VERIFICATION_ERROR.getMessage(core));
            return new VerificationResult(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage(core));
        }
    }

    private UUID getRepeatUuid(User userData, YggdrasilService yggdrasilService) throws SQLException {
        UUID ret = userData.getRedirectUuid();
        if (core.getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(ret).size() == 0) {
            return ret;
        }
        UUID newUuid = UUID.randomUUID();
        core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_REPEAT_UUID.getMessage(
                core,
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
    public List<List<YggdrasilService>> getVeriOrder(String name) throws SQLException {
        List<List<YggdrasilService>> ret = new ArrayList<>();
        Set<YggdrasilService> one = core.getSqlManager().getUserDataHandler().getYggdrasilServiceByCurrentName(name);
        one.removeIf(yggdrasilService -> !yggdrasilService.isEnable());
        List<YggdrasilService> two = new ArrayList<>();
        for (YggdrasilService serviceEntry : core.getYggdrasilServicesHandler().getServices()) {
            if (!serviceEntry.isEnable()) continue;
            if (!one.isEmpty() && one.contains(serviceEntry)) continue;
            two.add(serviceEntry);
        }
        if (!one.isEmpty())
            ret.add(new ArrayList<>(one));
        ret.add(two);
        return ret;
    }
}
