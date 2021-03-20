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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserProperty;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.http.HttpGetter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class SkinRepairHandler {

    private static boolean repairThirdPartySkin(UserProperty property, UserProperty.Property onlineProperty) throws Exception {
        if(property.getProperty().equals(onlineProperty)) return false;
        String skin = Optional.ofNullable(new JsonParser().parse(new String(onlineProperty.getDecoderValue())))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("textures"))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("SKIN"))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("url"))
                .map(JsonElement::getAsString)
                .map(url -> "url=" + URLEncoder.encode(url, StandardCharsets.UTF_8))
                .orElse("");
        if(PluginData.isEmpty(skin)) return true;
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", skin, 3);

        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if(value.has("data")){
            JsonObject data = Optional.ofNullable(value.get("data"))
                    .map(JsonElement::getAsJsonObject)
                    .map(jsonObject -> jsonObject.get("texture"))
                    .map(JsonElement::getAsJsonObject).orElse(null);
            if(data != null){
                property.setProperty(onlineProperty);
                property.setRepair_property(new UserProperty.Property(data.get("value").getAsString(),  data.get("signature").getAsString()));
            }
        }
        return true;
    }

    public static boolean isVanilla(String value){
        String skinUrl = Optional.ofNullable(new JsonParser().parse(new String(Base64.getDecoder().decode(value))).getAsJsonObject())
                .map(jsonObject -> jsonObject.get("textures"))
                .map(element -> element.getAsJsonObject().get("SKIN"))
                .map(JsonElement::getAsString).orElse("");
        return skinUrl.startsWith("https://sessionserver.mojang.com/session/minecraft/profile");
    }

    public static UserProperty repairThirdPartySkin(UUID onlineUuid, String name, String value, String signature) throws Exception {
        boolean newUserEntry;
        UserProperty userProperty;
        newUserEntry = (userProperty = SQLHandler.getUserPropertyByOnlineUuid(onlineUuid)) == null;

        if(repairThirdPartySkin(userProperty == null ? userProperty = new UserProperty() : userProperty, new UserProperty.Property(value, signature))){
            if(newUserEntry){
                SQLHandler.writeNewUserProperty(userProperty);
            } else {
                SQLHandler.updateUserProperty(userProperty);
            }
        }
        return userProperty;
    }
}
