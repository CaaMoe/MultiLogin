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
import moe.caa.multilogin.core.http.HttpGetter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Optional;

public class SkinRepairHandler {
    private static PublicKey publicKey = null;

    public static void setSignaturePublicKey(PublicKey publicKey){
        SkinRepairHandler.publicKey = publicKey;
    }

    public static Property repairSkin(Property property) throws IOException {
        boolean value = true;
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(property.value.getBytes());
            value = signature.verify(property.getDecoderSignature());
        } catch (Exception ignore){
        }

        return value ? property : repairThirdPartySkin(property);
    }

    private static Property repairThirdPartySkin(Property property) throws IOException {
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
        String response = HttpGetter.httpPost("https://api.mineskin.org/generate/url", skin);
        JsonObject value = new JsonParser().parse(response).getAsJsonObject();
        if(value.has("data")){
            Optional<JsonObject> optData = Optional.ofNullable(value.get("data"))
                    .map(JsonElement::getAsJsonObject)
                    .map(jsonObject -> jsonObject.get("texture"))
                    .map(JsonElement::getAsJsonObject);
            if(optData.isPresent()){
                property.value = optData.get().get("value").getAsString();
                property.signature = optData.get().get("signature").getAsString();
                return property;
            }
        }
        throw new RuntimeException(String.format("生成有效签名皮肤失败，API数据： %s", value));
    }

    public static class Property{
        private String name;
        private String value;
        private String signature;

        public Property(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public byte[] getDecoderSignature(){
            // TODO: 2021/3/15  property.signature contains '\n' ?
            return Base64.getDecoder().decode(signature);
        }

        public byte[] getDecoderValue(){
            // TODO: 2021/3/15  property.value contains '\n' ?
            return Base64.getDecoder().decode(value);
        }
    }
}
