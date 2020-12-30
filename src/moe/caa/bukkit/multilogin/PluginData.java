package moe.caa.bukkit.multilogin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import moe.caa.bukkit.multilogin.yggdrasil.MLGameProfile;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class PluginData {
    public static Map<String, YggdrasilServiceSection> Services = new HashMap<>();
    public static final File configFile = new File(MultiLogin.INSTANCE.getDataFolder(), "config.yml");
    public static final File configSwapUuid = new File(MultiLogin.INSTANCE.getDataFolder(), "swap_uuid.json");
    public static final File configUser = new File(MultiLogin.INSTANCE.getDataFolder(), "user.json");
    public static YamlConfiguration configurationConfig = null;

    private static final Set<YggdrasilServiceSection> serviceSet = new HashSet<>();
    private static final Map<UUID, UUID> swapUuidMap = new HashMap<>();
    private static boolean whitelist;
    private static final Set<UserEntry> userMap = new HashSet<>();
    private static final List<String> cacWhitelist = new LinkedList<>();

    private static void genFile() throws IOException {
        MultiLogin login = MultiLogin.INSTANCE;
        if(!login.getDataFolder().exists() && !login.getDataFolder().mkdirs()){
            throw new IOException("无法创建配置文件夹: " + login.getDataFolder().getPath());
        }
        MultiLogin.INSTANCE.saveDefaultConfig();
        if(!configSwapUuid.exists() && !configSwapUuid.createNewFile()){
            throw new IOException("无法创建文件" + configSwapUuid.getPath());
        }
        if(!configUser.exists() && !configUser.createNewFile()){
            throw new IOException("无法创建文件" + configUser.getPath());
        }
    }

    public static void reloadConfig() throws IOException {
        genFile();
        MultiLogin.INSTANCE.reloadConfig();
        serviceSet.clear();
        configurationConfig = (YamlConfiguration) MultiLogin.INSTANCE.getConfig();
        Logger log = MultiLogin.INSTANCE.getLogger();
        ConfigurationSection services = configurationConfig.getConfigurationSection("services");
        if(services != null){
            for(String path : services.getKeys(false)){
                YggdrasilServiceSection section = YggdrasilServiceSection.fromYaml(path, services.getConfigurationSection(path));
                if(section != null){
                    serviceSet.add(section);
                } else {
                    log.severe("无效的Yggdrasil验证服务器： " + path);
                }
            }
        }
        if (isOfficialYgg()) {
            log.info("已设置启用正版验证");
            YggdrasilServiceSection.OFFICIAL = new YggdrasilServiceSection("", getOfficialName(), "", getOfficialConvUuid(), false);
        } else {
            log.info("已设置不启用正版验证");
        }
        log.info(String.format("已成功载入%d个Yggdrasil验证服务器", serviceSet.size() + (isOfficialYgg() ? 1 : 0)));
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
        JsonObject json = (JsonObject) new JsonParser().parse(new FileReader(configSwapUuid));
        for(Map.Entry<String, JsonElement> entry : json.entrySet()){
            try {
                UUID from = UUID.fromString(entry.getKey());
                UUID to = UUID.fromString(entry.getValue().getAsString());
                swapUuidMap.put(from, to);
            } catch (Exception ignored) {
                log.severe(String.format("损坏的数据 %s:%s  来自文件%s", entry.getKey(), entry.getValue().toString(), configSwapUuid.getName()));
            }
        }
        log.info(String.format("成功读取%d条uuid转化数据", swapUuidMap.size()));

        // TODO 忽略格式检查
        userMap.clear();
        JsonObject json1 = (JsonObject) new JsonParser().parse(new FileReader(configUser));
        whitelist = json1.get("whitelist").getAsBoolean();
        JsonArray array = json1.get("data").getAsJsonArray();
        for(JsonElement je : array){
            boolean flag = false;
            try {
                UserEntry entry = UserEntry.fromJson(je.getAsJsonArray());
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
    }

    public static synchronized void saveData() throws IOException {
        genFile();
        JsonObject json = new JsonObject();
        for(Map.Entry<UUID, UUID> entry : swapUuidMap.entrySet()){
            json.addProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        JsonWriter jw = new JsonWriter(new FileWriter(configSwapUuid));
        jw.setIndent("  ");
        MultiLogin.GSON.toJson(json, jw);
        jw.flush();
        jw.close();
    }

    public static boolean isOfficialYgg() {
        return configurationConfig.getBoolean("officialServices", true);
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

    public static TextComponent getUserVerificationMessage(MLGameProfile profile){
        String name = profile.getName();
        YggdrasilServiceSection yggServer = profile.getYggService();
        if(yggServer == null){
            return new TextComponent("验证未通过，请重试！");
        }
        UserEntry current = null;

        for(UserEntry entry : userMap){
            if(entry.getUuid().equals(profile.getId())){
                if(!entry.getYggServer().equals(yggServer.getPath())){
                    return new TextComponent("你只能通过指定的验证方式加入游戏！");
                }
                current = entry;
                continue;
            }
            if(isNoRepeatedName() && entry.getName().equalsIgnoreCase(name)){
                return new TextComponent("您的ID被抢注");
            }
        }
        if(current != null){
            current.setName(name);
        } else {
            current = new UserEntry(profile.getId(), name, yggServer.getPath(), false);
        }
        if (isWhitelist() && !current.whitelist && !cacWhitelist.remove(name)){
            return new TextComponent("你没有白名单，快爬");
        }
        userMap.add(current);
        return null;
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
                    if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(yggServer)){
                        return new UserEntry(uuid, name, yggServer, whitelist);
                    }
                }
            } catch (Exception ignore){}
            return null;
        }
    }
}
