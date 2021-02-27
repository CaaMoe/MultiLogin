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

/**
 * 处理插件数据
 */
public class PluginData {
    public static IConfiguration configurationConfig = null;
    private static IConfiguration defaultConfigurationConfig = null;

    private static final File cacheWhitelistFile = new File(MultiCore.getPlugin().getPluginDataFolder(), "cache_whitelist.json");
    private static final Set<String> cacheWhitelist = Collections.synchronizedSet(new HashSet<>());

    private static final Set<YggdrasilServiceEntry> serviceSet = new HashSet<>();
    private static boolean whitelist = true;

    /**
     * 初始化
     */
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

    /**
     * 生成插件数据文件夹
     */
    private static void genFile() throws IOException {
        if(!MultiCore.getPlugin().getPluginDataFolder().exists() && !MultiCore.getPlugin().getPluginDataFolder().mkdirs()){
            throw new IOException(String.format("无法创建配置文件夹: %s",  MultiCore.getPlugin().getPluginDataFolder().getPath()));
        }
        MultiCore.getPlugin().savePluginDefaultConfig();
    }

    /**
     * 重新加载配置和白名单文件
     */
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

    /**
     * 移除某名未曾进入服务器的玩家的白名单权限
     * @param name 玩家名称或uuid
     * @return 移除结果
     */
    public static boolean removeCacheWhitelist(String name) {
        boolean ret = cacheWhitelist.remove(name);
        if(ret){
            saveWhitelist();
        }
        return ret;
    }

    /**
     * 添加白名单
     * @param name 玩家名称或uuid
     * @return 添加结果
     */
    public static boolean addCacheWhitelist(String name) {
        boolean ret = cacheWhitelist.add(name);
        if(ret){
            saveWhitelist();
        }
        return ret;
    }

    /**
     * 测试文本消息是否正确，并且将不正确的文本消息设置为默认值
     */
    private static void testMsg(Logger log, String path, Object... args){
        try {
            String.format(configurationConfig.getString(path), args);
        }catch (Exception ignore){
            configurationConfig.set(path, defaultConfigurationConfig.getString(path));
            log.warning(String.format("无效的节点 %s 已恢复默认值", path));
        }
    }

    /**
     * 获得是否启用正版验证
     * @return 是否启用正版验证
     */
    public static boolean isOfficialYgg() {
        return configurationConfig.getBoolean("officialEnable", false);
    }

    /**
     * 获得是否启用正版白名单
     * @return 是否启用正版白名单
     */
    public static boolean isOfficialYggWhitelist() {
        return configurationConfig.getBoolean("officialServicesWhitelist", true);
    }

    /**
     * 获得设置拥有ID保护功能的Yggdrasil服务器的path
     * @return 设置拥有ID保护功能的Yggdrasil服务器的path
     */
    public static String getSafeIdService(){
        return configurationConfig.getString("safeId", "");
    }

    /**
     * 获得Yggdrasil验证超时时间
     * @return Yggdrasil验证超时时间
     */
    public static long getTimeOut(){
        return configurationConfig.getLong("servicesTimeOut", 7000);
    }

    /**
     * 获得正版验证服务器的别称
     * @return 正版验证服务器的别称
     */
    private static String getOfficialName(){
        return configurationConfig.getString("officialName", "Official");
    }

    /**
     * 获得当前所有Yggdrasil验证服务器
     * @return 当前所有Yggdrasil验证服务器
     */
    public static Set<YggdrasilServiceEntry> getServiceSet() {
        return serviceSet;
    }

    /**
     * 获得正版验证服务器的UUID生成规则
     * @return 正版验证服务器的UUID生成规则
     */
    private static ConvUuid getOfficialConvUuid(){
        try {
            ConvUuid ret;
            ret = ConvUuid.valueOf(configurationConfig.getString("officialConvUuid"));
            return ret;
        }catch (Exception ignore){}
        MultiCore.getPlugin().getPluginLogger().severe("无法读取配置文件节点 officialConvUuid ，已应用为默认值 DEFAULT.");
        return ConvUuid.DEFAULT;
    }

    /**
     * 获得是否开启全局白名单
     * @return 是否开启全局白名单
     */
    public static boolean isWhitelist() {
        return whitelist;
    }

    /**
     * 设置是否开启全局白名单
     * @param whitelist 是否开启全局白名单
     */
    public synchronized static void setWhitelist(boolean whitelist) {
        PluginData.whitelist = whitelist;
        saveWhitelist();
    }

    /**
     * 判断一个字符串是否为空
     * 版本类库中没有稳定的地址
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 通过Yggdrasil服务器的path检索Yggdrasil服务器对象
     * @param path Yggdrasil服务器的path
     * @return 检索到的Yggdrasil服务器对象
     */
    public static YggdrasilServiceEntry getYggdrasilServerEntry(String path){
        for (YggdrasilServiceEntry serviceEntry : serviceSet){
            if(serviceEntry.getPath().equalsIgnoreCase(path)){
                return serviceEntry;
            }
        }
        return null;
    }

    /**
     * 保存数据
     */
    public static void close(){
        try {
            SQLHandler.close();
        } catch (SQLException ignored) { }
        saveWhitelist();
    }

    /**
     * 保存白名单数据
     */
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

    /**
     * 通过configuration获得SQL链接
     * @param configuration SQL配置
     * @return 链接参数
     */
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
