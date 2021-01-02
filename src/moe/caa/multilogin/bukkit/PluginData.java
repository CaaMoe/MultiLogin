package moe.caa.multilogin.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import moe.caa.multilogin.bukkit.yggdrasil.MLGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginData {
    public static final File configFile = new File(MultiLogin.INSTANCE.getDataFolder(), "config.yml");
    public static final File configSwapUuid = new File(MultiLogin.INSTANCE.getDataFolder(), "swap_uuid.json");
    public static final File configUser = new File(MultiLogin.INSTANCE.getDataFolder(), "user.json");
    public static YamlConfiguration configurationConfig = null;
    public static YamlConfiguration defaultConfigurationConfig = null;

    private static final Set<YggdrasilServiceSection> serviceSet = new HashSet<>();
    private static final Map<UUID, UUID> swapUuidMap = new HashMap<>();
    private static boolean whitelist;
    private static final Set<UserEntry> userMap = new HashSet<>();
    private static final Set<String> cacWhitelist = new HashSet<>();

    private static void genFile() throws IOException {
        MultiLogin login = MultiLogin.INSTANCE;
        if(!login.getDataFolder().exists() && !login.getDataFolder().mkdirs()){
            throw new IOException(String.format("无法创建配置文件夹: %s",  login.getDataFolder().getPath()));
        }
        MultiLogin.INSTANCE.saveDefaultConfig();
        if(!configSwapUuid.exists() && !configSwapUuid.createNewFile()){
            throw new IOException(String.format("无法创建文件: %s", configSwapUuid.getPath()));
        }
        if(!configUser.exists() && !configUser.createNewFile()){
            throw new IOException(String.format("无法创建文件: %s", configUser.getPath()));
        }
    }

    public static void reloadConfig() throws IOException {
        genFile();
        try {
            defaultConfigurationConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(MultiLogin.INSTANCE.getResource("config.yml")));
        } catch (Exception ignore){}
        MultiLogin.INSTANCE.reloadConfig();
        serviceSet.clear();
        configurationConfig = (YamlConfiguration) MultiLogin.INSTANCE.getConfig();
        Logger log = MultiLogin.INSTANCE.getLogger();
        ConfigurationSection services = configurationConfig.getConfigurationSection("services");
        if(services != null){
            for(String path : services.getKeys(false)){
                if(path.equalsIgnoreCase("official") || path.equalsIgnoreCase("multi") ){
                    log.warning("请勿将official或multi值设置于验证服务器标记名称处，该节点所定义的Yggdrasil服务器失效!");
                    continue;
                }
                YggdrasilServiceSection section = YggdrasilServiceSection.fromYaml(path, services.getConfigurationSection(path));
                if(section != null){
                    serviceSet.add(section);
                } else {
                    log.severe(String.format("无效的Yggdrasil验证服务器： %s", path));
                }
            }
        }
        if (isOfficialYgg()) {
            log.info("已设置启用正版验证");
            YggdrasilServiceSection.OFFICIAL = new YggdrasilServiceSection("official", getOfficialName(), "", getOfficialConvUuid(),isOfficialYggWhitelist(), false);
        } else {
            log.info("已设置不启用正版验证");
        }
        log.info(String.format("已成功载入%d个Yggdrasil验证服务器", serviceSet.size() + (isOfficialYgg() ? 1 : 0)));
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

    public static UUID getSwapUUID(UUID uuid, YggdrasilServiceSection yggdrasilServiceSection, String name){
        UUID ret = swapUuidMap.get(uuid);
        if(ret == null){
            if (yggdrasilServiceSection.getConvUuid() == YggdrasilServiceSection.ConvUuid.DEFAULT) {
                ret = uuid;
            } else {
                ret = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            }
            swapUuidMap.put(uuid, ret);
        }
        return ret;
    }

    public static synchronized void readData() throws IOException {
        genFile();
        swapUuidMap.clear();
        Logger log = MultiLogin.INSTANCE.getLogger();
        try{
            JsonObject json = (JsonObject) new JsonParser().parse(new FileReader(configSwapUuid));
            if(json != null && json.entrySet() != null){
                for(Map.Entry<String, JsonElement> entry : json.entrySet()){
                    try {
                        UUID from = UUID.fromString(entry.getKey());
                        UUID to = UUID.fromString(entry.getValue().getAsString());
                        swapUuidMap.put(from, to);
                    } catch (Exception ignored) {
                        log.severe(String.format("损坏的数据 %s:%s  来自文件%s", entry.getKey(), entry.getValue().toString(), configSwapUuid.getName()));
                    }
                }
            }
        } catch (Exception ignore){}

        log.info(String.format("成功读取%d条uuid转化数据", swapUuidMap.size()));

        userMap.clear();
        cacWhitelist.clear();
        JsonObject json1 = new JsonObject();
        try{
            json1 = (JsonObject) new JsonParser().parse(new FileReader(configUser));
            JsonArray array = json1.get("data").getAsJsonArray();
            for(JsonElement je : array){
                boolean flag = false;
                try {
                    UserEntry entry = UserEntry.fromJson(je.getAsJsonObject());
                    if(entry != null){
                        flag = true;
                        userMap.add(entry);
                    }
                }catch (Exception ignore){
                }
                if(!flag){
                    log.severe(String.format("损坏的数据 %s 来自文件%s", je.toString(), configUser.getName()));
                }
            }
        } catch (Exception ignore){}
        JsonElement wl = json1.get("whitelist");
        whitelist = wl != null && !wl.isJsonNull() && wl.getAsBoolean();
        try{
            JsonArray array = json1.get("cacWhitelist").getAsJsonArray();
            for(JsonElement je : array){
                cacWhitelist.add(je.getAsString());
            }
        } catch (Exception ignore){}

        log.info(String.format("载入%d+%d条用户数据", userMap.size(), cacWhitelist.size()));

    }

    public static synchronized void saveData() throws IOException {
        genFile();
        Logger log = MultiLogin.INSTANCE.getLogger();
        try {
            JsonObject json = new JsonObject();
            for(Map.Entry<UUID, UUID> entry : swapUuidMap.entrySet()){
                json.addProperty(entry.getKey().toString(), entry.getValue().toString());
            }
            JsonWriter jw = new JsonWriter(new FileWriter(configSwapUuid));
            jw.setIndent("  ");
            MultiLogin.GSON.toJson(json, jw);
            jw.flush();
            jw.close();
        } catch (Exception ignore){
            log.severe(String.format("无法保存数据文件: %s", configSwapUuid.getName()));
        }
        try {
            JsonObject json = new JsonObject();
            json.addProperty("whitelist", whitelist);
            JsonArray array = new JsonArray();
            for(String name : cacWhitelist){
                array.add(name);
            }
            json.add("cacWhitelist", array);
            JsonArray userArray = new JsonArray();
            for(UserEntry entry : userMap){
                userArray.add(entry.getJson());
            }
            json.add("data", userArray);
            JsonWriter jw = new JsonWriter(new FileWriter(configUser));
            jw.setIndent("  ");
            MultiLogin.GSON.toJson(json, jw);
            jw.flush();
            jw.close();
        } catch (Exception ignore){
        log.severe(String.format("无法保存数据文件: %s", configUser.getName()));
        }
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
        return configurationConfig.getBoolean("officialServices", true);
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

    public static Set<YggdrasilServiceSection> getServiceSet() {
        return serviceSet;
    }

    private static YggdrasilServiceSection.ConvUuid getOfficialConvUuid(){
        try {
            YggdrasilServiceSection.ConvUuid ret;
            ret = YggdrasilServiceSection.ConvUuid.valueOf(configurationConfig.getString("officialConvUuid"));
            return ret;
        }catch (Exception ignore){}
        MultiLogin.INSTANCE.getLogger().severe("无法读取配置文件节点 officialConvUuid ，已应用为默认值 DEFAULT.");
        return YggdrasilServiceSection.ConvUuid.DEFAULT;
    }

    public static boolean isWhitelist() {
        return whitelist;
    }

    public static void setWhitelist(boolean whitelist) {
        PluginData.whitelist = whitelist;
    }

    public static boolean addWhitelist(String name){
        for(UserEntry entry : userMap){
            if (entry.getName().equalsIgnoreCase(name)) {
                if(entry.whitelist){
                    return false;
                }
                entry.whitelist = true;
                return true;
            }
        }
        return cacWhitelist.add(name);
    }

    public static boolean removeWhitelist(String name){
        UUID uuid = getUUID(name);
        for(UserEntry entry : userMap){
            if (entry.getName().equalsIgnoreCase(name) || (uuid != null && uuid.equals(entry.uuid))) {
                Player player = Bukkit.getPlayer(entry.getUuid());
                if(player != null){
                    player.kickPlayer(configurationConfig.getString("msgDelWhitelistInGame"));
                }
                if(!entry.whitelist){
                    return false;
                }
                entry.whitelist = false;
                return true;
            }
        }
        return cacWhitelist.remove(name);
    }

    public static List<String> listWhitelist(){
        List<String> ret = userMap.stream().filter(UserEntry::isWhitelist).map(UserEntry::getName).collect(Collectors.toList());
        ret.addAll(cacWhitelist);
        return ret;
    }

    public static String getUserVerificationMessage(MLGameProfile profile){
        String name = profile.getName();
        UUID uuid = profile.getOnlineUuid();
        YggdrasilServiceSection yggServer = profile.getYggService();
        if(yggServer == null){
            return configurationConfig.getString("msgNoAdopt");
        }
        UserEntry current = null;

        for(UserEntry entry : userMap){
            if(entry.getUuid().equals(profile.getOnlineUuid())){
                if(!entry.getYggServer().equals(yggServer.getPath())){
                    return configurationConfig.getString("msgNoChae");
                }
                current = entry;
                continue;
            }
            if(isNoRepeatedName() && entry.getName().equalsIgnoreCase(name) && !getSafeIdService().equalsIgnoreCase(yggServer.getPath())){
                return configurationConfig.getString("msgRushName");
            }
        }
        if(current != null){
            current.setName(name);
        } else {
            current = new UserEntry(profile.getOnlineUuid(), name, yggServer.getPath(), false);
        }

        if(isWhitelist()){
            if(!current.whitelist & !(cacWhitelist.remove(name) | cacWhitelist.remove(uuid.toString()))){
                return configurationConfig.getString("msgNoWhitelist");
            }
            current.whitelist = true;
        }

        if(!current.whitelist & yggServer.isWhitelist() & !(cacWhitelist.remove(name) | cacWhitelist.remove(uuid.toString()))){
            return configurationConfig.getString("msgNoWhitelist");
        }
        userMap.add(current);
        return null;
    }

    public static UserEntry getUserEntry(String name){
        for(UserEntry entry : userMap){
            if(entry.name.equalsIgnoreCase(name)){
                return entry;
            }
        }
        return null;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static UUID getUUID(String s){
        try {
            return UUID.fromString(s);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Configuration getConfigurationConfig() {
        return configurationConfig;
    }

    static class UserEntry {
        private final UUID uuid;
        private String name;
        private final String yggServer;
        private boolean whitelist;

        UserEntry(UUID uuid, String name, String yggServer, boolean whitelist) {
            this.uuid = uuid;
            this.name = name;
            this.yggServer = yggServer;
            this.whitelist = whitelist;
        }

        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getYggServer() {
            return yggServer;
        }

        public String getYggServerDisplayName() {
            for(YggdrasilServiceSection section : serviceSet){
                if(section.getPath().equals(yggServer)){
                    return section.getName();
                }
            }
            return yggServer;
        }

        public boolean isWhitelist() {
            return whitelist;
        }

        public void setWhitelist(boolean whitelist) {
            this.whitelist = whitelist;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JsonElement getJson(){
            JsonObject ret = new JsonObject();
            ret.addProperty("uuid", uuid.toString());
            ret.addProperty("name", name);
            ret.addProperty("yggServer", yggServer);
            ret.addProperty("whitelist", whitelist);
            return ret;
        }

        public static UserEntry fromJson(JsonElement json){
            try {
                if(json instanceof JsonObject){
                    JsonObject root = (JsonObject) json;
                    UUID uuid = UUID.fromString(root.get("uuid").getAsString());
                    String name = root.get("name").getAsString();
                    String yggServer = root.get("yggServer").getAsString();
                    boolean whitelist = root.get("whitelist").getAsBoolean();
                    if(!PluginData.isEmpty(name) && !PluginData.isEmpty(yggServer)){
                        return new UserEntry(uuid, name, yggServer, whitelist);
                    }
                }
            } catch (Exception ignore){
            }
            return null;
        }
    }
}
