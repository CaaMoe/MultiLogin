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
import moe.caa.multilogin.core.http.HttpGetter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SkinRepairHandler {

    private static UserProperty repairThirdPartySkin(UserProperty property) throws IOException {
        String skin = Optional.ofNullable(new JsonParser().parse(new String(property.getDecoderValue())))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("textures"))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("SKIN"))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("url"))
                .map(JsonElement::getAsString)
                .map(url -> "url=" + URLEncoder.encode(url, StandardCharsets.UTF_8))
                .orElseThrow(()-> new RuntimeException("no skin"));
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", skin, 3);

        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if(value.has("data")){
            JsonObject data = Optional.ofNullable(value.get("data"))
                    .map(JsonElement::getAsJsonObject)
                    .map(jsonObject -> jsonObject.get("texture"))
                    .map(JsonElement::getAsJsonObject).orElse(null);
            if(data != null){
                JsonElement entry;
                property.setValue((entry = data.get("value")).isJsonPrimitive() ? entry.getAsString() : null);
                property.setSignature((entry = data.get("signature")).isJsonPrimitive() ? entry.getAsString() : null);
                return property;
            }
        }
        // TODO: 2021/3/20 I18N Message
        throw new RuntimeException(String.format("生成有效签名皮肤失败，API数据： %s", value));
    }
}
