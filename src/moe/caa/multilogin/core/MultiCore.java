package moe.caa.multilogin.core;

import com.google.gson.*;
import net.md_5.bungee.api.chat.TextComponent;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiCore {
    private static IPlugin plugin;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String preV = null;
    private static String relV = null;
    private static final BASE64Decoder decoder = new BASE64Decoder();
    private static final boolean CURRENT_PRE_VERSION = true;

    public static IConfiguration getConfig(){
        return plugin.getPluginConfig();
    }

    public static IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException {
        return plugin.yamlLoadConfiguration(reader);
    }

    public static InputStream getResource(String path){
        return plugin.getPluginResource(path);
    }

    public static void kickPlayer(UUID uuid, String msg) {
        plugin.kickPlayer(uuid, msg);
    }

    public static IPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(IPlugin plugin) {
        MultiCore.plugin = plugin;
        plugin.runTaskAsyncTimer(MultiCore::update, 0, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(MultiCore::setUpUpdate, 0);
        plugin.runTaskAsyncTimer(MultiCore::save, 0, 20 * 60);
        try {
            PluginData.reloadConfig();
            PluginData.readData();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getMLPluginLogger().severe("无法读取配置或数据文件，请检查！");
            plugin.setPluginEnabled(false);
        }
    }

    public static void save(){
        try {
            PluginData.saveData();
        } catch (IOException e) {
            plugin.getMLPluginLogger().severe("无法保存数据文件");
            e.printStackTrace();
        }
    }

    public static void submitCommand(String cmd, ISender sender, String[] strings){
        if(cmd.equalsIgnoreCase("whitelist")){
            if(strings.length > 0){
                if(strings[0].equalsIgnoreCase("add")){
                    if(strings.length == 2){
                        Command.executeAdd(sender, strings);
                        return;
                    }
                } else if(strings[0].equalsIgnoreCase("remove")){
                    if(strings.length == 2){
                        Command.executeRemove(sender, strings);
                        return;
                    }
                } else if(strings[0].equalsIgnoreCase("on")){
                    if(strings.length == 1){
                        Command.executeOn(sender);
                        return;
                    }
                } else if(strings[0].equalsIgnoreCase("off")){
                    if(strings.length == 1){
                        Command.executeOff(sender);
                        return;
                    }
                } else if(strings[0].equalsIgnoreCase("list")){
                    if(strings.length == 1){
                        Command.executeList(sender);
                        return;
                    }
                }
            }
        } else if(cmd.equalsIgnoreCase("multilogin")){
            if(strings.length > 0){
                if(strings[0].equalsIgnoreCase("query")){
                    if(strings.length <= 2){
                        Command.executeQuery(sender, strings);
                        return;
                    }
                } else if(strings[0].equalsIgnoreCase("reload")){
                    if(strings.length == 1){
                        Command.executeReload(sender);
                        return;
                    }
                }
            }
        }
        sender.sendMessage(new TextComponent(PluginData.getConfigurationConfig().getString("msgInvCmd")));
    }

    public static List<String> suggestCommand(String cmd, ISender sender, String[] strings){
        if(cmd.equalsIgnoreCase("whitelist")){
            if(sender.isOp() || sender.hasPermission("multilogin.whitelist.tab")){
                if(strings.length == 1){
                    return Stream.of("add", "remove", "on", "off", "list").filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
                }
                if(strings.length == 2){
                    if(strings[0].equalsIgnoreCase("remove")){
                        return PluginData.listWhitelist().stream().filter(s1 -> s1.startsWith(strings[1])).collect(Collectors.toList());
                    }
                }
            }
        } else if(cmd.equalsIgnoreCase("multilogin")){
            if(sender.isOp() || sender.hasPermission("multilogin.multilogin.tab")){
                if(strings.length == 1){
                    return Stream.of("query", "reload").filter(s1 -> s1.startsWith(strings[0])).collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    public static boolean isUpdate(){
        if(preV == null && relV == null){
            return false;
        }
        if(preV == null && CURRENT_PRE_VERSION){
            return true;
        }
        if(preV != null && CURRENT_PRE_VERSION && !preV.endsWith(getCurrentV())){
            return true;
        }
        if(relV != null && !CURRENT_PRE_VERSION && !relV.endsWith(getCurrentV())){
            return true;
        }
        return false;
    }

    public static String getPreV() {
        return preV;
    }

    public static String getRelV() {
        return relV;
    }

    public static String getCurrentV(){
        return plugin.getVersion();
    }


    private static void setUpUpdate(){
        update();
        if (isUpdate()) {
            plugin.getMLPluginLogger().info("插件有新的版本发布");
            plugin.getMLPluginLogger().info(String.format("当前版本为 %s", getCurrentV()));
            plugin.getMLPluginLogger().info(String.format("最新预发布版本为 %s", preV));
            plugin.getMLPluginLogger().info(String.format("最新发布版本为 %s", relV));
        }
    }

    private static void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/version.json?ref=master");
            JsonObject jo = (JsonObject) new JsonParser().parse(httpGet(url));
            String v = new String(decoder.decodeBuffer(jo.get("content").getAsString()));
            JsonObject content = (JsonObject) new JsonParser().parse(v);
            Set<Map.Entry<String, JsonElement>> set = content.entrySet();
            String preV = null;
            String relV = null;
            for (Map.Entry<String, JsonElement> entry : set) {
                if(entry.getKey().equalsIgnoreCase("pre-release")){
                    preV = entry.getValue().getAsString();
                } else  if(entry.getKey().equalsIgnoreCase("release")){
                    relV = entry.getValue().getAsString();
                }
            }
            MultiCore.preV = preV;
            MultiCore.relV = relV;
        } catch (Exception ignore){}
    }

    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
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

    public static String httpPost(String url, String arg) throws IOException {
        return httpPost(new URL(url), arg);
    }

    public static String httpPost(URL url, String arg) throws IOException {
        StringBuilder result = new StringBuilder();
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        PrintWriter pw = new PrintWriter(connection.getOutputStream());
        pw.write(arg);
        pw.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
