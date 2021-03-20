/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.data.UserProperty
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.data;

import java.util.Base64;
import java.util.UUID;

public class UserProperty {
    private UUID onlineUuid;
    private String value;
    private String signature;
    private String mineSkinValue;
    private String mineSkinSignature;

    public UserProperty(UUID onlineUuid, String value, String signature, String mineSkinValue, String mineSkinSignature) {
        this.onlineUuid = onlineUuid;
        this.value = value;
        this.signature = signature;
        this.mineSkinValue = mineSkinValue;
        this.mineSkinSignature = mineSkinSignature;
    }

    public UUID getOnlineUuid() {
        return onlineUuid;
    }

    public void setOnlineUuid(UUID onlineUuid) {
        this.onlineUuid = onlineUuid;
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

    public String getMineSkinValue() {
        return mineSkinValue;
    }

    public void setMineSkinValue(String mineSkinValue) {
        this.mineSkinValue = mineSkinValue;
    }

    public String getMineSkinSignature() {
        return mineSkinSignature;
    }

    public void setMineSkinSignature(String mineSkinSignature) {
        this.mineSkinSignature = mineSkinSignature;
    }

    public byte[] getDecoderSignature(){
        return Base64.getDecoder().decode(signature);
    }

    public byte[] getDecoderValue(){
        return Base64.getDecoder().decode(value);
    }

    public byte[] getDecoderMineSkinSignature(){
        return Base64.getDecoder().decode(mineSkinSignature);
    }

    public byte[] getDecoderMineSkinValue(){
        return Base64.getDecoder().decode(mineSkinValue);
    }
}
