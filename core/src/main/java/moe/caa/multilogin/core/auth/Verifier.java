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

    public Verifier() {
    }

    public VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilService yggdrasilService) {
        try {
            User userData = MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(onlineUuid);

            boolean updUserEntry = userData != null;

            // 验证服务器不符
            if (updUserEntry) {
                if (!Objects.equals(userData.getYggdrasilService(), yggdrasilService.getPath())) {
                    MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.VERIFICATION_NO_CHAE.getMessage(currentName, onlineUuid.toString(), yggdrasilService.getName(), yggdrasilService.getPath(), userData.getYggdrasilService()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_CHAE.getMessage());
                }
            }

            //名称规范检查
            String reg = ValueUtil.notIsEmpty(yggdrasilService.getNameAllowedRegular()) ?
                    yggdrasilService.getNameAllowedRegular() : ValueUtil.notIsEmpty(MultiCore.getInstance().nameAllowedRegular) ?
                    "" : MultiCore.getInstance().nameAllowedRegular;

            if (!reg.isEmpty() && !Pattern.matches(reg, currentName)) {
                MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_PAT_MATCH.getMessage(currentName, onlineUuid.toString(), reg));
                return new VerificationResult(LanguageKeys.VERIFICATION_NO_PAT_MATCH.getMessage());
            }

            //重名检查
            if (!MultiCore.getInstance().safeId.contains(yggdrasilService.getPath())) {
                List<User> repeatedNameUserEntries = MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(currentName);
                for (User repeatedNameUserEntry : repeatedNameUserEntries) {
                    if (!repeatedNameUserEntry.equals(userData)) {
                        MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_RUSH_NAME.getMessage(currentName, onlineUuid.toString(), repeatedNameUserEntry.getOnlineUuid()));
                        return new VerificationResult(LanguageKeys.VERIFICATION_RUSH_NAME.getMessage());
                    }
                }
            }

            userData = !updUserEntry ? new User(onlineUuid, currentName, yggdrasilService.getConvUuid().getResultUuid(onlineUuid, currentName), yggdrasilService.getPath(), false, MultiCore.getInstance().getYggdrasilServicesHandler()) : userData;
            userData.setCurrentName(currentName);

            if (!updUserEntry) {
                if (yggdrasilService.isConvRepeat()) {
                    userData.setRedirectUuid(getRepeatUuid(userData, yggdrasilService));
                }
            }

            // 白名单检查
            if (!userData.isWhitelist() && yggdrasilService.isWhitelist()) {
                if (!(MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(currentName) | MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(onlineUuid.toString()))) {
                    MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_NO_WHITELIST.getMessage(currentName, onlineUuid.toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_NO_WHITELIST.getMessage());
                }
                userData.setWhitelist(true);
            }

            if (updUserEntry) {
                MultiCore.getInstance().getSqlManager().getUserDataHandler().updateUserEntry(userData);
            } else {
                MultiCore.getInstance().getSqlManager().getUserDataHandler().writeNewUserEntry(userData);
            }

            // 重名踢出，返回重复 Redirect uuid 的 Sender 对象
            User finalUserData = userData;
            FutureTask<ISender> task = new FutureTask<>(() -> {
                for (ISender sender : MultiCore.getPlugin().getPlayer(currentName)) {
                    if (!sender.getPlayerUniqueIdentifier().equals(onlineUuid)) {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_RUSH_NAME_ONL.getMessage());
                    }
                }

                return MultiCore.getPlugin().getPlayer(finalUserData.getRedirectUuid());
            });


            // 等待主线程任务
            MultiCore.getPlugin().getSchedule().runTask(task);
            ISender sender = task.get();

            // 重复登入验证
            if (sender != null) {
                if (yggdrasilService.isRefuseRepeatedLogin()) {
                    MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_REPEATED_LOGIN.getMessage(userData.getCurrentName(), userData.getOnlineUuid().toString()));
                    return new VerificationResult(LanguageKeys.VERIFICATION_ANOTHER_LOGIN_SQUEEZE.getMessage());
                } else {
                    FutureTask<ISender> kick = new FutureTask<>(() -> {
                        sender.kickPlayer(LanguageKeys.VERIFICATION_SELF_LOGIN_SQUEEZE.getMessage());
                        return null;
                    });
                    MultiCore.getScheduler().runTask(kick);
                    kick.get();
                }
            }

            CACHE_LOGIN.put(userData.getRedirectUuid(), currentName);
            CACHE_USER.add(userData);
            MultiCore.log(LoggerLevel.INFO, LanguageKeys.VERIFICATION_ALLOW.getMessage(userData.getOnlineUuid().toString(), userData.getCurrentName(), userData.getRedirectUuid().toString(), yggdrasilService.getName(), yggdrasilService.getPath()));
            return new VerificationResult(userData.getRedirectUuid(), userData);
        } catch (Exception e) {
            MultiCore.log(LoggerLevel.ERROR, e);
            MultiCore.log(LoggerLevel.ERROR, LanguageKeys.VERIFICATION_ERROR.getMessage());
            return new VerificationResult(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    private UUID getRepeatUuid(User userData, YggdrasilService yggdrasilService) throws SQLException {
        UUID ret = userData.getRedirectUuid();
        if (MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(ret).size() == 0) {
            return ret;
        }
        UUID newUuid = UUID.randomUUID();
        MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_VERIFICATION_REPEAT_UUID.getMessage(
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
        Set<YggdrasilService> one = MultiCore.getInstance().getSqlManager().getUserDataHandler().getYggdrasilServiceByCurrentName(name);
        one.removeIf(yggdrasilService -> !yggdrasilService.isEnable());
        List<YggdrasilService> two = new ArrayList<>();
        for (YggdrasilService serviceEntry : MultiCore.getInstance().getYggdrasilServicesHandler().getServices()) {
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
