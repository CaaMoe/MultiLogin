package moe.caa.multilogin.core;

import com.google.gson.*;
import moe.caa.multilogin.core.auth.VerificationResult;
import moe.caa.multilogin.core.data.PluginData;
import moe.caa.multilogin.core.data.SQLHandler;
import moe.caa.multilogin.core.data.UserEntry;
import moe.caa.multilogin.core.data.YggdrasilServiceEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.FutureTask;

import static moe.caa.multilogin.core.data.PluginData.configurationConfig;

public class MultiCore {
    private static IPlugin plugin = null;
    private static String relV = null;
    private static final Base64.Decoder decoder = Base64.getDecoder();

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean initService(IPlugin plugin){
        MultiCore.plugin = plugin;
        plugin.runTaskAsyncTimer(MultiCore::update, 20 * 60 * 60 * 12, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(MultiCore::setUpUpdate, 0);
        try {
            PluginData.initService();
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void disable(){
        PluginData.close();
    }

    public static IPlugin getPlugin() {
        return plugin;
    }

    public static boolean isUpdate(){
        try {
            return !plugin.getVersion().endsWith(relV);
        } catch (Exception ignore){}
        return false;
    }

    private static void setUpUpdate(){
        update();
        if (isUpdate()) {
            plugin.getPluginLogger().info("=======================================================");
            plugin.getPluginLogger().info(String.format("插件有新版本发布，当前版本为 %s，最新版本为 %s", plugin.getVersion(), relV));
            plugin.getPluginLogger().info("=======================================================");
        }
    }

    private static void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
            JsonObject jo = (JsonObject) new JsonParser().parse(httpGet(url));
            String pat = jo.get("content").getAsString();
            pat = pat.substring(0, pat.length() - 1);
            String v = new String(decoder.decode(pat), StandardCharsets.UTF_8);
            JsonObject content = (JsonObject) new JsonParser().parse(v);
            Set<Map.Entry<String, JsonElement>> set = content.entrySet();
            String relV = null;
            for (Map.Entry<String, JsonElement> entry : set) {
                if(entry.getKey().equalsIgnoreCase("release")){
                    relV = entry.getValue().getAsString();
                }
            }
            MultiCore.relV = relV;
        } catch (Exception ignore){}
    }

    public static VerificationResult getUserVerificationMessage(UUID onlineUuid, String currentName, YggdrasilServiceEntry yggdrasilService){
        try {
            boolean updUserEntry;
            if(yggdrasilService == null){
                return new VerificationResult(configurationConfig.getString("msgNoAdopt"));
            }
            UserEntry userData = SQLHandler.getUserEntryByOnlineUuid(onlineUuid);

            if(updUserEntry = userData != null){
                if(!PluginData.isEmpty(userData.getYggdrasil_service())){
                    if(!userData.getYggdrasil_service().equals(yggdrasilService.getPath())){
                        return new VerificationResult(configurationConfig.getString("msgNoChae"));
                    }
                }
            }

            if(!PluginData.getSafeIdService().equalsIgnoreCase(yggdrasilService.getPath())){
                List<UserEntry> repeatedNameUserEntries = SQLHandler.getUserEntryByCurrentName(currentName);
                for (UserEntry repeatedNameUserEntry : repeatedNameUserEntries) {
                    if(!repeatedNameUserEntry.equals(userData)){
                        return new VerificationResult(configurationConfig.getString("msgRushName"));
                    }
                }
            }

            userData = !updUserEntry ? new UserEntry(onlineUuid.toString(), currentName, yggdrasilService.getConvUuid().getResultUuid(onlineUuid, currentName).toString(), yggdrasilService.getPath(), 0) : userData;
            userData.setCurrent_name(currentName);

            if(userData.getWhitelist() == 0){
                if(!(PluginData.removeCacheWhitelist(currentName) | PluginData.removeCacheWhitelist(onlineUuid.toString()))){
                    return new VerificationResult(configurationConfig.getString("msgNoWhitelist"));
                }
                userData.setWhitelist(1);
            }

            if(updUserEntry){
                SQLHandler.updateUserEntry(userData);
            } else {
                SQLHandler.writeNewUserEntry(userData);
            }

            FutureTask<String> task = new FutureTask<>(() -> {
                for (Map.Entry<UUID, String> entry : MultiCore.getPlugin().getOnlineList().entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(currentName) && !entry.getKey().equals(onlineUuid)) {
                        MultiCore.getPlugin().kickPlayer(entry.getKey(), configurationConfig.getString("msgRushNameOnl"));
                    }
                }
                return null;
            });
            plugin.runTask(task, 0);
            task.get();

            plugin.getPluginLogger().info(String.format("uuid: %s, 来自玩家: %s, 验证服务器: %s", userData.getRedirect_uuid(), currentName, yggdrasilService.getName()));
            return new VerificationResult(userData.getRedirect_uuid());
        } catch (Exception e){
            e.printStackTrace();
            getPlugin().getPluginLogger().severe("验证遭到异常");
            return new VerificationResult(configurationConfig.getString("msgNoAdopt"));
        }
    }

    public static List<List<YggdrasilServiceEntry>> getVeriOrder(String name) throws SQLException {
        List<List<YggdrasilServiceEntry>> ret = new ArrayList<>();
        List<YggdrasilServiceEntry> one = new ArrayList<>();
        List<YggdrasilServiceEntry> two = new ArrayList<>();
        Set<YggdrasilServiceEntry> cac = new HashSet<>();
        List<UserEntry> repeatedNameUserEntries = SQLHandler.getUserEntryByCurrentName(name);
        for (UserEntry userEntry : repeatedNameUserEntries) {
            if (cac.add(userEntry.getServiceEntry())) {
                one.add(userEntry.getServiceEntry());
            }
        }
        for(YggdrasilServiceEntry serviceEntry : PluginData.getServiceSet()){
            if (cac.add(serviceEntry)) {
                two.add(serviceEntry);
            }
        }

        ret.add(one);
        ret.add(two);
        return ret;
    }

    public static String httpGet(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
    }

    public static boolean submitCommand(String whitelist, ISender bukkitSender, String[] strings) {
        return false;
    }

    public static List<String> suggestCommand(String whitelist, ISender bukkitSender, String[] strings) {
        return null;
    }
}
