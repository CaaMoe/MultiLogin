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

    public UserProperty(UUID onlineUuid, String value, String signature) {
        this.onlineUuid = onlineUuid;
        this.value = value;
        this.signature = signature;
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

    public byte[] getDecoderSignature(){
        // TODO: 2021/3/15  property.signature contains '\n' ?
        return Base64.getDecoder().decode(signature);
    }

    public byte[] getDecoderValue(){
        // TODO: 2021/3/15  property.value contains '\n' ?
        return Base64.getDecoder().decode(value);
    }
}
