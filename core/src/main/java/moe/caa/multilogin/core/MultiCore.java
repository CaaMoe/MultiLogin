package moe.caa.multilogin.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.auth.VerificationResult;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static moe.caa.multilogin.core.data.data.PluginData.configurationConfig;
import static moe.caa.multilogin.core.data.data.PluginData.getTimeOut;

/**
 * 插件核心类
 */
public class MultiCore {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static IPlugin plugin = null;
    private static String relV = null;

    /**
     * 启动服务
     *
     * @param plugin 插件对象
     * @return 是否加载成功
     */
    public static boolean initService(IPlugin plugin) {
        MultiCore.plugin = plugin;
//        自动更新
        plugin.runTaskAsyncTimer(MultiCore::update, 20 * 60 * 60 * 12, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(MultiCore::setUpUpdate, 0);
        try {
            PluginData.initService();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭插件
     */
    public static void disable() {
        PluginData.close();
        HttpAuth.shutDown();
    }

    /**
     * 获得插件对象
     *
     * @return 当前插件对象
     */
    public static IPlugin getPlugin() {
        return plugin;
    }

    /**
     * 判断插件是否有更新
     *
     * @return 是否有更新
     */
    public static boolean isUpdate() {
        try {
            return !plugin.getVersion().endsWith(relV);
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     * 发送更新消息
     */
    private static void setUpUpdate() {
        update();
        if (isUpdate()) {
            plugin.getPluginLogger().info("=======================================================");
            plugin.getPluginLogger().info(String.format("插件有新版本发布，当前版本为 %s，最新版本为 %s", plugin.getVersion(), relV));
            plugin.getPluginLogger().info("=======================================================");
        }
    }

    /**
     * 周期性的更新检查
     */
    private static void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
            JsonObject jo = (JsonObject) new JsonParser().parse(httpGet(url));
            String pat = jo.get("content").getAsString();
            pat = pat.substring(0, pat.length() - 1);
            String v = new String(decoder.decode(pat), StandardCharsets.UTF_8);
            MultiCore.relV = v.split("\\s+")[2];
        } catch (Exception ignore) {
        }
    }

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
            plugin.runTask(task, 0);
            task.get();

            plugin.getPluginLogger().info(String.format("uuid: %s, 来自玩家: %s, 验证服务器: %s(%s)", userData.getRedirect_uuid(), currentName, yggdrasilService.getName(), yggdrasilService.getPath()));
            return new VerificationResult(userData.getRedirect_uuid());
        } catch (Exception e) {
            e.printStackTrace();
            getPlugin().getPluginLogger().severe("验证遭到异常");
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

    /**
     * 向指定的url发送GET请求
     *
     * @param url 指定的url
     * @return 请求结果
     */
    public static String httpGet(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout((int) getTimeOut());
        connection.setReadTimeout((int) getTimeOut());
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * 向指定的url发送GET请求
     *
     * @param url 指定的url
     * @return 请求结果
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
    }

    /**
     * 执行一个插件命令
     *
     * @param cmd     根命令
     * @param sender  命令执行者
     * @param strings 命令参数
     * @return 命令执行结果
     */
    public static boolean submitCommand(String cmd, ISender sender, String[] strings) {
        try {
            if (cmd.equalsIgnoreCase("whitelist")) {
                if (strings.length > 0)
                    if (strings[0].equalsIgnoreCase("add")) {
                        if (strings.length == 2) {
                            CommandHandler.executeAdd(sender, strings);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("remove")) {
                        if (strings.length == 2) {
                            CommandHandler.executeRemove(sender, strings);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("on")) {
                        if (strings.length == 1) {
                            CommandHandler.executeOn(sender);
                            return true;
                        }
                    } else if (strings[0].equalsIgnoreCase("off")) {
                        if (strings.length == 1) {
                            CommandHandler.executeOff(sender);
                            return true;
                        }
                    }
            } else if (cmd.equalsIgnoreCase("multilogin") &&
                    strings.length > 0) {
                if (strings[0].equalsIgnoreCase("query")) {
                    if (strings.length <= 2) {
                        CommandHandler.executeQuery(sender, strings);
                        return true;
                    }
                } else if (strings[0].equalsIgnoreCase("reload") &&
                        strings.length == 1) {
                    CommandHandler.executeReload(sender);
                    return true;
                }
            }
            sender.sendMessage(new TextComponent(configurationConfig.getString("msgInvCmd")));
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getPluginLogger().severe("执行命令时出现异常");
            sender.sendMessage(new TextComponent(ChatColor.RED + "执行命令时出现异常"));
        }
        return true;
    }

    /**
     * 请求一个命令建议
     *
     * @param cmd     根命令
     * @param sender  命令发送者
     * @param strings 参数
     * @return 建议列表
     */
    public static List<String> suggestCommand(String cmd, ISender sender, String[] strings) {
        if (cmd.equalsIgnoreCase("whitelist")) {
            if (sender.isOp() || sender.hasPermission("multilogin.whitelist.tab")) {
                if (strings.length == 1) {
                    return Stream.of(new String[]{"add", "remove", "on", "off"}).filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
                }
            }
        } else if (cmd.equalsIgnoreCase("multilogin") && (
                sender.isOp() || sender.hasPermission("multilogin.multilogin.tab")) &&
                strings.length == 1) {
            return Stream.of(new String[]{"query", "reload"}).filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
