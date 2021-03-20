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
import moe.caa.multilogin.core.data.data.UserProperty;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.http.HttpGetter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                // TODO: 2021/3/20 I18N Message
                .orElseThrow(()-> new RuntimeException("no skin"));
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", skin, 3);

        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if(value.has("data")){
            JsonObject data = Optional.ofNullable(value.get("data"))
                    .map(JsonElement::getAsJsonObject)
                    .map(jsonObject -> jsonObject.get("texture"))
                    .map(JsonElement::getAsJsonObject).orElse(null);
            if(data != null){
                property.setProperty(onlineProperty);
                property.setRepair_property(new UserProperty.Property(data.get("name").getAsString(), data.get("value").getAsString(),  data.get("signature").getAsString()));
                return true;
            }
        }
        // TODO: 2021/3/20 I18N Message
        throw new RuntimeException(String.format("生成有效签名皮肤失败，API数据： %s", value));
    }

    public static UserProperty repairThirdPartySkin(UUID onlineUuid, String name, String value, String signature) throws Exception {
        boolean newUserEntry;
        UserProperty userProperty;
        newUserEntry = (userProperty = SQLHandler.getUserPropertyByOnlineUuid(onlineUuid)) == null;

        if(repairThirdPartySkin(userProperty == null ? userProperty = new UserProperty() : userProperty, new UserProperty.Property(name, value, signature))){
            if(newUserEntry){
                SQLHandler.writeNewUserProperty(userProperty);
            } else {
                SQLHandler.updateUserProperty(userProperty);
            }
        }
        return userProperty;
    }
}
