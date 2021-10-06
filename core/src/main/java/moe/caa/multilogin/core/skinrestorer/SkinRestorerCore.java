package moe.caa.multilogin.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.SkinRestorerRuleEnum;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

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
    public void doRestorer(YggdrasilAuthResult result, HasJoinedResponse response) {
        // 未启用跳过
        if (result.getService().getSkinRestorer() == SkinRestorerRuleEnum.OFF) return;
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
                    apply(textures.get(), Property.builder()
                            .signature(entry.getCurrentTextureSignature())
                            .value(entry.getCurrentTextureValue())
                            .build()
                    );
                    return;
                }
            }
            CountDownLatch latch = new CountDownLatch(1);

            String finalSkinModel = skinModel;
            core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                try {
                    Property property = doRestorer(finalSkinModel, skinUrl, result.getService().getSkinRestorerRetry());
                    apply(textures.get(), property);
                    JsonObject write = new JsonObject();
                    write.addProperty("value", property.getValue());
                    write.addProperty("signature", property.getSignature());

                    if (entry != null) {
                        entry.setRestorer_data(MultiCore.getGson().toJson(write));
                        core.getSqlManager().getSkinRestorerDataHandler().updateRestorerEntry(entry);
                    } else {
                        RestorerEntry restorerEntry = new RestorerEntry(response.getId(), skinUrl, MultiCore.getGson().toJson(write));
                        core.getSqlManager().getSkinRestorerDataHandler().writeNewRestorerEntry(restorerEntry);
                    }

                } catch (Exception e) {
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "An exception occurs when repairing the skin.", e);
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "result: " + result);
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "response: " + response);
                } finally {
                    latch.countDown();
                }
            });
            if (result.getService().getSkinRestorer() == SkinRestorerRuleEnum.LOGIN) latch.await();
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "An exception occurs when repairing the skin.", e);
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "response: " + response);
        }
    }

    /**
     * 进行网络修复
     *
     * @param skinModel 皮肤模型，未指定则为经典模型
     * @param skinUrl   第三方皮肤URL
     * @return 修复后带有签名的空name数据
     * @throws Exception 修复时异常
     */
    private Property doRestorer(String skinModel, String skinUrl, int retry) throws Exception {
        JsonObject jo = new JsonObject();
        jo.addProperty("name", UUID.randomUUID().toString().substring(0, 6));
        jo.addProperty("variant", skinModel == null ? "classic" : skinModel);
        jo.addProperty("visibility", 0);
        jo.addProperty("url", skinUrl);
        String s = HttpUtil.httpPostJson(new URL("https://api.mineskin.org/generate/url"),
                MultiCore.getGson().toJson(jo), "application/json", (int) core.getConfig().getServicesTimeOut(), retry);
        JsonObject resultJson = JsonParser.parseString(s).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("texture");

        return Property.builder().name("")
                .value(resultJson.getAsJsonPrimitive("value").getAsString())
                .signature(resultJson.getAsJsonPrimitive("signature").getAsString()).build();
    }

    /**
     * 应用皮肤数据
     *
     * @param target 目标
     * @param value  皮肤数据
     */
    private void apply(Property target, Property value) {
        target.setSignature(value.getSignature());
        target.setValue(value.getValue());
    }
}
