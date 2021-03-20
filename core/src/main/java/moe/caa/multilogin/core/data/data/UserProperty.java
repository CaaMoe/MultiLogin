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
    private Property property;
    private Property repair_property;

    public UserProperty(UUID onlineUuid, Property property, Property repair_property) {
        this.onlineUuid = onlineUuid;
        this.property = property;
        this.repair_property = repair_property;
    }

    public UserProperty() {
    }

    public UUID getOnlineUuid() {
        return onlineUuid;
    }

    public void setOnlineUuid(UUID onlineUuid) {
        this.onlineUuid = onlineUuid;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Property getRepair_property() {
        return repair_property;
    }

    public void setRepair_property(Property repair_property) {
        this.repair_property = repair_property;
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

        public Property() {
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
    }
}
