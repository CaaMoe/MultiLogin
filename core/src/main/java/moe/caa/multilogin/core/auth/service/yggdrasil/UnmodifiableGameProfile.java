package moe.caa.multilogin.core.auth.service.yggdrasil;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.profile.Property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnmodifiableGameProfile extends GameProfile {

    public UnmodifiableGameProfile(UUID id, String name, Map<String, Property> propertyMap) {
        super(id, name, propertyMap);
    }

    @Override
    public void setId(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPropertyMap(Map<String, Property> propertyMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Property> getPropertyMap() {
        Map<String, Property> map = new HashMap<>();
        for (Map.Entry<String, Property> entry : super.getPropertyMap().entrySet()) {
            map.put(entry.getKey(), UnmodifiableProperty.unmodifiable(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    public static UnmodifiableGameProfile unmodifiable(GameProfile profile){
        return new UnmodifiableGameProfile(profile.getId(), profile.getName(), profile.getPropertyMap());
    }

    public static class UnmodifiableProperty extends Property {
        public UnmodifiableProperty(String name, String value, String signature) {
            super(name, value, signature);
        }

        public static UnmodifiableProperty unmodifiable(Property property){
            return new UnmodifiableProperty(property.getName(), property.getValue(), property.getSignature());
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSignature(String signature) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
