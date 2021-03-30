/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.skin.SkinRepairHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserTextures;
import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;
import moe.caa.multilogin.core.data.databse.handler.TexturesDataHandler;
import moe.caa.multilogin.core.http.HttpGetter;

import java.util.Optional;
import java.util.UUID;

public class SkinRepairHandler {

    private static boolean repairThirdPartySkin(UserTextures property, UserTextures.Textures onlineTextures, YggdrasilServiceEntry serviceEntry) throws Exception {
        String skin = getSkinUrl(new String(onlineTextures.getDecoderValue()));
        String cacSkin = getSkinUrl(new String(Optional.of(property).map(UserTextures::getProperty).map(UserTextures.Textures::getDecoderValue).orElse(new byte[0])));

// 判断当前皮肤url是否为空 或 判断当前皮肤url是官方
        if (PluginData.isEmpty(skin) || skin.contains("minecraft.net")) {
            property.setRepair_property(onlineTextures);
            property.setProperty(onlineTextures);
            return false;
        }

// 判断是否缓存过
        if (skin.equalsIgnoreCase(cacSkin))
            return false;

// 修复
        JsonObject jo = new JsonObject();
        jo.addProperty("url", skin);
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", jo.toString(), serviceEntry.getSkinRepairRetry());

// 写入数据
        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if (value.has("data")) {
            JsonObject data = value.get("data").getAsJsonObject().get("texture").getAsJsonObject();
            if (!data.has("signature"))
                return false;
            property.setProperty(onlineTextures);
            property.setRepair_property(new UserTextures.Textures(data.get("value").getAsString(), data.get("signature").getAsString()));
            return true;
        }
        return false;
    }

    private static String getSkinUrl(String root) {
        try {
            return new JsonParser().parse(root).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
        } catch (Exception ignore) {
        }
        return "";
    }

    public static UserTextures repairThirdPartySkin(UUID onlineUuid, String value, String signature, YggdrasilServiceEntry serviceEntry) throws Exception {

// 判断是否启用该功能
        if (!PluginData.isOpenSkinRepair())
            return new UserTextures(onlineUuid, null, new UserTextures.Textures(value, signature));
        boolean newUserEntry;
        UserTextures userTextures = TexturesDataHandler.getUserPropertyByOnlineUuid(onlineUuid);
        newUserEntry = userTextures == null;

// 新建皮肤档案
        userTextures = userTextures == null ? new UserTextures(onlineUuid, new UserTextures.Textures(), new UserTextures.Textures()) : userTextures;

// 修复皮肤
        repairThirdPartySkin(userTextures, new UserTextures.Textures(value, signature), serviceEntry);
        userTextures.setProperty(new UserTextures.Textures(value, signature));
        if (newUserEntry) {
            TexturesDataHandler.writeNewUserProperty(userTextures);
        } else {
            TexturesDataHandler.updateUserProperty(userTextures);
        }
        return userTextures;
    }
}
