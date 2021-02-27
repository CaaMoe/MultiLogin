package moe.caa.multilogin.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.MultiCore;

import java.io.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class PluginData {
    public static IConfiguration configurationConfig = null;
    private static IConfiguration defaultConfigurationConfig = null;

    private static final File cacheWhitelistFile = new File(MultiCore.getPlugin().getPluginDataFolder(), "cache_whitelist.json");
    private static final Set<String> cacheWhitelist = Collections.synchronizedSet(new HashSet<>());

    private static final Set<YggdrasilServiceEntry> serviceSet = new HashSet<>();
    private static boolean whitelist = true;

    public static void initService() throws Exception {
        genFile();
        if(!cacheWhitelistFile.exists() && !cacheWhitelistFile.createNewFile()){
            throw new IOException(String.format("无法创建文件: %s", cacheWhitelistFile.getPath()));
        }
        reloadConfig();

        String[] args = getSqlUrl(configurationConfig.getConfigurationSection("sql"));

        try {
            SQLHandler.init(args);
        } catch (Exception e) {
            MultiCore.getPlugin().getPluginLogger().info("连接到数据库时出现异常");
            throw e;
        }
        MultiCore.getPlugin().getPluginLogger().info(String.format("链接到数据库： %s", args.length == 1 ? "SQLite" : "MySQL"));
    }

    private static void genFile() throws IOException {
        if(!MultiCore.getPlugin().getPluginDataFolder().exists() && !MultiCore.getPlugin().getPluginDataFolder().mkdirs()){
            throw new IOException(String.format("无法创建配置文件夹: %s",  MultiCore.getPlugin().getPluginDataFolder().getPath()));
        }
        MultiCore.getPlugin().savePluginDefaultConfig();
    }

    public static void reloadConfig() throws IOException {
        serviceSet.clear();
        MultiCore.getPlugin().reloadPluginConfig();

        defaultConfigurationConfig = MultiCore.getPlugin().yamlLoadConfiguration(new InputStreamReader(MultiCore.getPlugin().getPluginResource("config.yml")));
        configurationConfig = MultiCore.getPlugin().getPluginConfig();

        Logger log = MultiCore.getPlugin().getPluginLogger();
        IConfiguration services = configurationConfig.getConfigurationSection("services");
        if(services != null){
            for(String path : services.getKeys(false)){
                if(path.equalsIgnoreCase("official")){
                    log.warning("请勿将official值设置于验证服务器标记名称处，该节点所定义的Yggdrasil服务器失效!");
                    continue;
                }
                YggdrasilServiceEntry section = YggdrasilServiceEntry.fromYaml(path, services.getConfigurationSection(path));
                if(section != null){
                    if(section.isEnable()) {
                        if (section.isEnable()) {
                            serviceSet.add(section);
                        }
                    }
                } else {
                    log.severe(String.format("无效的Yggdrasil验证服务器： %s", path));
                }
            }
        }
        if (isOfficialYgg()) {
            YggdrasilServiceEntry.OFFICIAL_YGG.setName(getOfficialName());
            YggdrasilServiceEntry.OFFICIAL_YGG.setConvUuid(getOfficialConvUuid());
            YggdrasilServiceEntry.OFFICIAL_YGG.setWhitelist(isOfficialYggWhitelist());
            serviceSet.add(YggdrasilServiceEntry.OFFICIAL_YGG);
            log.info("已启用正版验证");
        }
        log.info(String.format("成功载入%d个Yggdrasil验证服务器", serviceSet.size()));

        cacheWhitelist.clear();

        try {
            JsonObject json = (JsonObject) new JsonParser().parse(new FileReader(cacheWhitelistFile));
            whitelist = Optional.ofNullable(json.get("whitelist_enable")).map(JsonElement::getAsBoolean).orElse(true);
            JsonArray array = json.get("cache_whitelist").getAsJsonArray();
            for (JsonElement element : array) {
                cacheWhitelist.add(element.getAsString());
            }
        } catch (Exception ignored){}

        testMsg(log, "msgNoAdopt");
        testMsg(log, "msgNoChae");
        testMsg(log, "msgRushName");
        testMsg(log, "msgNoWhitelist");
        testMsg(log, "msgNoPermission");
        testMsg(log, "msgAddWhitelist", "null");
        testMsg(log, "msgAddWhitelistAlready", "null");
        testMsg(log, "msgDelWhitelistInGame");
        testMsg(log, "msgDelWhitelist", "null");
        testMsg(log, "msgDelWhitelistAlready", "null");
        testMsg(log, "msgOpenWhitelist");
        testMsg(log, "msgOpenWhitelistAlready");
        testMsg(log, "msgCloseWhitelist");
        testMsg(log, "msgCloseWhitelistAlready");
        testMsg(log, "msgWhitelistListNoth");
        testMsg(log, "msgWhitelistListN", 0, "null");
        testMsg(log, "msgYDQuery", "null", "null");
        testMsg(log, "msgYDQueryNoRel" ,"null");
        testMsg(log, "msgReload");
        testMsg(log, "msgNoPlayer");
        testMsg(log, "msgRushNameOnl");
    }

    public static boolean removeCacheWhitelist(String currentName) {
        boolean ret = cacheWhitelist.remove(currentName);
        if(ret){
            saveWhitelist();
        }
        return ret;
    }

    public static boolean addCacheWhitelist(String currentName) {
        boolean ret = cacheWhitelist.add(currentName);
        if(ret){
            saveWhitelist();
        }
        return ret;
    }

    private static void testMsg(Logger log, String path, Object... args){
        try {
            String.format(configurationConfig.getString(path), args);
        }catch (Exception ignore){
            configurationConfig.set(path, defaultConfigurationConfig.getString(path));
            log.warning(String.format("无效的节点 %s 已恢复默认值", path));
        }
    }

    public static boolean isOfficialYgg() {
        return configurationConfig.getBoolean("officialEnable", false);
    }

    public static boolean isOfficialYggWhitelist() {
        return configurationConfig.getBoolean("officialServicesWhitelist", true);
    }

    public static String getSafeIdService(){
        return configurationConfig.getString("safeId", "");
    }

    public static long getTimeOut(){
        return configurationConfig.getLong("servicesTimeOut", 7000);
    }

    public static boolean isNoRepeatedName() {
        return configurationConfig.getBoolean("noRepeatedName", true);
    }

    private static String getOfficialName(){
        return configurationConfig.getString("officialName", "Official");
    }

    public static Set<YggdrasilServiceEntry> getServiceSet() {
        return serviceSet;
    }

    private static ConvUuid getOfficialConvUuid(){
        try {
            ConvUuid ret;
            ret = ConvUuid.valueOf(configurationConfig.getString("officialConvUuid"));
            return ret;
        }catch (Exception ignore){}
        MultiCore.getPlugin().getPluginLogger().severe("无法读取配置文件节点 officialConvUuid ，已应用为默认值 DEFAULT.");
        return ConvUuid.DEFAULT;
    }

    public static boolean isWhitelist() {
        return whitelist;
    }

    public synchronized static void setWhitelist(boolean whitelist) {
        PluginData.whitelist = whitelist;
        saveWhitelist();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static YggdrasilServiceEntry getYggdrasilServerEntry(String path){
        for (YggdrasilServiceEntry serviceEntry : serviceSet){
            if(serviceEntry.getPath().equalsIgnoreCase(path)){
                return serviceEntry;
            }
        }
        return null;
    }

    public static void close(){
        try {
            SQLHandler.close();
        } catch (SQLException ignored) { }
        saveWhitelist();
    }

    public synchronized static void saveWhitelist() {
        try {
            genFile();
            JsonObject root = new JsonObject();
            root.addProperty("whitelist_enable", whitelist);
            JsonArray array = new JsonArray();
            for (String s : cacheWhitelist) {
                array.add(s);
            }
            root.add("cache_whitelist", array);

            JsonWriter jw = new JsonWriter(new FileWriter(cacheWhitelistFile));
            jw.setIndent("  ");
            MultiCore.GSON.toJson(root, jw);
            jw.flush();
            jw.close();
        } catch (Exception e){
            e.printStackTrace();
            MultiCore.getPlugin().getPluginLogger().severe(String.format("无法保存文件: %s",  cacheWhitelistFile.getPath()));
        }
    }

    private static String[] getSqlUrl(IConfiguration configuration) throws Exception {
        if(configuration!= null){
            if("SQLITE".equalsIgnoreCase(configuration.getString("backend"))){
                Class.forName("org.sqlite.JDBC");
                return new String[]{String.format("jdbc:sqlite:%s%s", MultiCore.getPlugin().getPluginDataFolder().getAbsolutePath(), "/multilogin.db")};
            }
            if("MYSQL".equalsIgnoreCase(configuration.getString("backend"))){
                Class.forName("com.mysql.jdbc.Driver");
                return new String[]{String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true",
                        configuration.getString("mysqlIp"),
                        configuration.getString("mysqlPort"),
                        configuration.getString("mySqlDatabase")
                ), configuration.getString("mySqlUsername"), configuration.getString("mySqlPassword")};
            }
        }
        return null;
    }
}
