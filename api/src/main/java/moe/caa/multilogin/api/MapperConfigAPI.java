package moe.caa.multilogin.api;

import java.util.LinkedHashMap;
import java.util.Map;

public interface MapperConfigAPI {
    Map<Integer,Integer> getPacketMapping();
    void save();
    void reload();
}
