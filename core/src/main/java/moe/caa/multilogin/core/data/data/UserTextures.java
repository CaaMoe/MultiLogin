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
import java.util.Objects;
import java.util.UUID;

public class UserTextures {
    private UUID onlineUuid;
    private Textures textures;
    private Textures repair_textures;

    public UserTextures(UUID onlineUuid, Textures textures, Textures repair_textures) {
        this.onlineUuid = onlineUuid;
        this.textures = textures;
        this.repair_textures = repair_textures;
    }

    public UserTextures() {
    }

    public UUID getOnlineUuid() {
        return onlineUuid;
    }

    public void setOnlineUuid(UUID onlineUuid) {
        this.onlineUuid = onlineUuid;
    }

    public Textures getProperty() {
        return textures;
    }

    public void setProperty(Textures textures) {
        this.textures = textures;
    }

    public Textures getRepair_property() {
        return repair_textures;
    }

    public void setRepair_property(Textures repair_textures) {
        this.repair_textures = repair_textures;
    }

    public static class Textures {
        private String value;
        private String signature;

        public Textures(String value, String signature) {
            this.value = value;
            this.signature = signature;
        }

        public Textures() {
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

        public byte[] getDecoderValue() {
            if (value == null) return new byte[0];
            return Base64.getDecoder().decode(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Textures textures = (Textures) o;
            return Objects.equals(value, textures.value) && Objects.equals(signature, textures.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, signature);
        }
    }
}
