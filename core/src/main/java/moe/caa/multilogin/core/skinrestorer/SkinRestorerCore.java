package moe.caa.multilogin.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代表皮肤修复核心<br>
 * 皮肤签名不符综合处理程序
 */
@AllArgsConstructor
public class SkinRestorerCore {
    private final MultiCore core;
    private final ConcurrentHashMap<UUID, UUID> doAsyncRestoring = new ConcurrentHashMap<>();

    /**
     * 开始进行皮肤修复
     *
     * @param response 登入结果
     */
    public void doRestorer(HasJoinedResponse response) {
        try {
            // 获得登入结果中的皮肤
            var textures = Optional.ofNullable(response.getPropertyMap())
                    .map(stringPropertyMap -> stringPropertyMap.get("textures"));
            // 空值跳过
            if (!textures.isPresent()) {
                return;
            }
            // 无效的皮肤 URL 跳过
            String skinUrl;
            String skinModel = null;
            try {
                var value = new String(ValueUtil.getDECODER().decode(textures.get().getValue()));
                JsonObject jsonObject = JsonParser.parseString(value).getAsJsonObject();
                jsonObject = jsonObject.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
                skinUrl = jsonObject.getAsJsonPrimitive("url").getAsString();
                if (jsonObject.has("metadata")) {
                    jsonObject = jsonObject.get("metadata").getAsJsonObject();
                    if (jsonObject.has("model")) {
                        skinModel = jsonObject.get("model").getAsString();
                    }
                }
            } catch (Exception e) {
                return;
            }
            // 无效的皮肤 URL 跳过
            URL url;
            try {
                url = new URL(skinUrl);
            } catch (Exception e) {
                return;
            }
            // 官方皮肤源跳过
            if (url.getHost().endsWith(".minecraft.net")) return;
            // 已经修复的跳过
            RestorerEntry entry = core.getSqlManager().getSkinRestorerDataHandler().getRestorerEntryByOnlineUuid(response.getId());
            if (entry != null) {
                if (entry.getCurrent_skin_url().equals(skinUrl)) {
                    textures.get().setSignature(entry.getCurrentTextureSignature());
                    textures.get().setValue(entry.getCurrentTextureValue());
                    return;
                }
            }

            // 异步修复，不延长登入时间
            String finalSkinModel = skinModel;
            core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                synchronized (doAsyncRestoring) {
                    if (doAsyncRestoring.containsKey(response.getId())) {
                        return;
                    }
                    doAsyncRestoring.put(response.getId(), response.getId());
                }

                JsonObject jo = new JsonObject();
                jo.addProperty("name", UUID.randomUUID().toString().substring(0, 6));
                jo.addProperty("variant", finalSkinModel == null ? "classic" : finalSkinModel);
                jo.addProperty("visibility", 0);
                jo.addProperty("url", skinUrl);

                try {
                    String s = HttpUtil.httpPostJson(new URL("https://api.mineskin.org/generate/url"),
                            MultiCore.getGson().toJson(jo), "application/json", (int) core.getConfig().getServicesTimeOut());

                    JsonObject resultJson = JsonParser.parseString(s).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("texture");

                    JsonObject write = new JsonObject();
                    write.addProperty("value", resultJson.getAsJsonPrimitive("value").getAsString());
                    write.addProperty("signature", resultJson.getAsJsonPrimitive("signature").getAsString());

                    if (entry != null) {
                        entry.setRestorer_data(MultiCore.getGson().toJson(write));
                        core.getSqlManager().getSkinRestorerDataHandler().updateRestorerEntry(entry);
                    } else {
                        RestorerEntry restorerEntry = new RestorerEntry(response.getId(), skinUrl, MultiCore.getGson().toJson(write));
                        core.getSqlManager().getSkinRestorerDataHandler().writeNewRestorerEntry(restorerEntry);
                    }

                } catch (Exception e) {
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "An exception occurs when repairing the skin.", e);
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "response: " + response);
                } finally {
                    doAsyncRestoring.remove(response.getId());
                }
            });
        } catch (Exception e){
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "An exception occurs when repairing the skin.", e);
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "response: " + response);
        }
    }
}
