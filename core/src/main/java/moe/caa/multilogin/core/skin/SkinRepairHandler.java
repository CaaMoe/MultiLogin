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
import moe.caa.multilogin.core.data.data.UserProperty;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.data.databse.handler.PropertyDataHandler;
import moe.caa.multilogin.core.http.HttpGetter;

import java.util.Optional;
import java.util.UUID;

public class SkinRepairHandler {

    private static boolean repairThirdPartySkin(UserProperty property, UserProperty.Property onlineProperty) throws Exception {
        String skin = getSkinUrl(new String(onlineProperty.getDecoderValue()));
        String cacSkin = getSkinUrl(new String(Optional.of(property).map(UserProperty::getProperty).map(UserProperty.Property::getDecoderValue).orElse(new byte[0])));

// 判断当前皮肤url是否为空 或 判断当前皮肤url是官方
        if(PluginData.isEmpty(skin) || skin.contains("minecraft.net")) {
            property.setRepair_property(onlineProperty);
            property.setProperty(onlineProperty);
            return false;
        }

// 判断是否缓存过
        if(skin.equalsIgnoreCase(cacSkin))
            return false;

// 修复
        JsonObject jo = new JsonObject();
        jo.addProperty("url", skin);
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", jo.toString(), (int) PluginData.configurationConfig.getLong("skinRepairRetry", 3));

// 写入数据
        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if(value.has("data")){
            JsonObject data = value.get("data").getAsJsonObject().get("texture").getAsJsonObject();
            if(!data.has("signature"))
                return false;
            property.setProperty(onlineProperty);
            property.setRepair_property(new UserProperty.Property(data.get("value").getAsString(),  data.get("signature").getAsString()));
            return true;
        }
        return false;
    }

    private static String getSkinUrl(String root){
        try {
            return new JsonParser().parse(root).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
        } catch (Exception ignore){}
        return "";
    }

    public static UserProperty repairThirdPartySkin(UUID onlineUuid, String value, String signature) throws Exception {

// 判断是否启用该功能
        if(!PluginData.isOpenSkinRepair()) return new UserProperty(onlineUuid, null, new UserProperty.Property(value, signature));
        boolean newUserEntry;
        UserProperty userProperty = PropertyDataHandler.getUserPropertyByOnlineUuid(onlineUuid);
        newUserEntry = userProperty == null;

// 新建皮肤档案
        userProperty = userProperty == null ? new UserProperty(onlineUuid, new UserProperty.Property(), new UserProperty.Property()) : userProperty;

// 修复皮肤
        repairThirdPartySkin(userProperty, new UserProperty.Property(value, signature));
        userProperty.setProperty(new UserProperty.Property(value, signature));
        if(newUserEntry){
            PropertyDataHandler.writeNewUserProperty(userProperty);
        } else {
            PropertyDataHandler.updateUserProperty(userProperty);
        }
        return userProperty;
    }
}
