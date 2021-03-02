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

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;
import moe.caa.multilogin.core.data.databse.SQLHandler;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;

import static moe.caa.multilogin.core.data.data.PluginData.configurationConfig;

public class Verifier {

    /**
     * 获得某名玩家的其他验证结果
     *
     * @param onlineUuid       在线UUID
     * @param currentName      当前名字
     * @param yggdrasilService 验证服务器对象
     * @return 验证结果
     */
    public static VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilServiceEntry yggdrasilService) {
        try {
            boolean updUserEntry;

            // 验证服务器为空
            if (yggdrasilService == null) {
                return new VerificationResult(configurationConfig.getString("msgNoAdopt"));
            }
            UserEntry userData = SQLHandler.getUserEntryByOnlineUuid(onlineUuid);

            // 验证服务器不符
            if (updUserEntry = userData != null) {
                if (!PluginData.isEmpty(userData.getYggdrasil_service())) {
                    if (!userData.getYggdrasil_service().equals(yggdrasilService.getPath())) {
                        return new VerificationResult(configurationConfig.getString("msgNoChae"));
                    }
                }
            }

            //重名检查
            if (!PluginData.getSafeIdService().equalsIgnoreCase(yggdrasilService.getPath())) {
                List<UserEntry> repeatedNameUserEntries = SQLHandler.getUserEntryByCurrentName(currentName);
                for (UserEntry repeatedNameUserEntry : repeatedNameUserEntries) {
                    if (!repeatedNameUserEntry.equals(userData)) {
                        return new VerificationResult(configurationConfig.getString("msgRushName"));
                    }
                }
            }

            userData = !updUserEntry ? new UserEntry(onlineUuid, currentName, yggdrasilService.getConvUuid().getResultUuid(onlineUuid, currentName), yggdrasilService.getPath(), 0) : userData;
            userData.setCurrent_name(currentName);

            // 白名单检查
            if (userData.getWhitelist() == 0 && yggdrasilService.isWhitelist()) {
                if (!(SQLHandler.removeCacheWhitelist(currentName) | SQLHandler.removeCacheWhitelist(onlineUuid.toString()))) {
                    return new VerificationResult(configurationConfig.getString("msgNoWhitelist"));
                }
                userData.setWhitelist(1);
            }

            if (updUserEntry) {
                SQLHandler.updateUserEntry(userData);
            } else {
                SQLHandler.writeNewUserEntry(userData);
            }

            // 重名踢出
            FutureTask<String> task = new FutureTask<>(() -> {
                for (Map.Entry<UUID, String> entry : MultiCore.getPlugin().getOnlineList().entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(currentName) && !entry.getKey().equals(onlineUuid)) {
                        MultiCore.getPlugin().kickPlayer(entry.getKey(), configurationConfig.getString("msgRushNameOnl"));
                    }
                }
                return null;
            });

            // 等待主线程任务
            MultiCore.getPlugin().runTask(task, 0);
            task.get();

            MultiCore.getPlugin().getPluginLogger().info(String.format("uuid: %s, 来自玩家: %s, 验证服务器: %s(%s)", userData.getRedirect_uuid(), currentName, yggdrasilService.getName(), yggdrasilService.getPath()));
            return new VerificationResult(userData.getRedirect_uuid());
        } catch (Exception e) {
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe("验证遭到异常");
            return new VerificationResult(configurationConfig.getString("msgNoAdopt"));
        }
    }

    /**
     * 通过玩家名字分批次访问Yggdrasil验证服务器
     *
     * @param name name
     * @return 验证服务器排序的结果
     */
    public static List<List<YggdrasilServiceEntry>> getVeriOrder(String name) throws SQLException {
        List<List<YggdrasilServiceEntry>> ret = new ArrayList<>();
//        第一批 缓存结果
        List<YggdrasilServiceEntry> one = SQLHandler.getYggdrasilServiceEntryByCurrentName(name);
//        第二批 非缓存
        List<YggdrasilServiceEntry> two = new ArrayList<>();
        Set<YggdrasilServiceEntry> cac = new HashSet<>(one);
        for (YggdrasilServiceEntry serviceEntry : PluginData.getServiceSet()) {
            if (!one.isEmpty())
                if (cac.contains(serviceEntry)) continue;
            two.add(serviceEntry);
        }
        if (!one.isEmpty())
            ret.add(one);
        ret.add(two);
        return ret;
    }

}
