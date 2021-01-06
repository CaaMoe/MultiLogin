package moe.caa.multilogin.bungee;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public class RefUtil {

    public static Field getField(Class clazz, Class target){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getType() == target){
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException();
    }

    public static Field getField(Class clazz, String target) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(target);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class clazz, String name, Class... args){
        for(Method method : clazz.getDeclaredMethods()){
            if(method.getName().equalsIgnoreCase(name)){
                if(Arrays.equals(method.getParameterTypes(), args)){
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static Object getEnumIns(Class clazz,String name) {
        for (Object constant : clazz.getEnumConstants()) {
            if(constant.toString().equalsIgnoreCase(name)){
                return constant;
            }
        }
        throw new IllegalArgumentException(name);
    }


    public static void initService() throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        MultiEncryptionResponse.init();

        Class<MultiEncryptionResponse> packetClass = MultiEncryptionResponse.class;
        int packetID = 0x01;

        Class<?> protocol_directionDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol_protocolDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");

        Field field_protocols = getField(protocol_directionDataClass, "protocols");
        Field field_TO_SERVER = getField(Protocol.class, "TO_SERVER");
        Field field_packetMap = getField(protocol_protocolDataClass, "packetMap");
        Field field_packetConstructors = getField(protocol_protocolDataClass, "packetConstructors");
        Object to_server = field_TO_SERVER.get(Protocol.LOGIN);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) field_protocols.get(to_server);
        for (int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            Object data = protocols.get(protocol);
            TObjectIntMap<Class<? extends DefinedPacket>> packetMap = (TObjectIntMap) field_packetMap.get(data);
            packetMap.remove(EncryptionResponse.class);
            packetMap.put(packetClass, packetID);
            Supplier<? extends DefinedPacket>[] constructors = (Supplier<? extends DefinedPacket>[]) field_packetConstructors.get(data);
            constructors[packetID] = MultiEncryptionResponse::new;
        }
    }
}
