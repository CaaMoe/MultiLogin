package moe.caa.multilogin.core.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class YamlConfig {
    private final Map<?, ?> contents;
    private static final Yaml YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(dumperOptions);
    }

    private YamlConfig(Map<?, ?> contents){
        this.contents = contents == null ? new LinkedHashMap<>() : contents;
    }

    public void save(Writer writer){
        YAML.dump(contents, writer);
    }

    public static YamlConfig fromReader(Reader reader){
        Map<?, ?> contents = YAML.loadAs(reader, LinkedHashMap.class);
        return new YamlConfig(contents);
    }
    
    private <T> Optional<T> caseVale(Optional<?> value, Class<T> type){
        if(value.isPresent() && value.get().getClass() == type){
            return (Optional<T>) value;
        }
        return Optional.empty();
    }
   
    private boolean hasValue(Optional<?> value, Class<?> type){
        if(value.isPresent()){
            if(type == value.get().getClass()){
                return true;
            }
        }
        return false;
    }
    
    public YamlConfig getSection(String path){
        return new YamlConfig((Map<?, ?>)get(path, Map.class).orElse(null));
    }
   
    public Optional<?> get(String path){
        return Optional.ofNullable(contents.get(path));
    }
    
    public <T> Optional<T> get(String path, Class<T> type){
        return caseVale(get(path) ,type);
    }
    
    public <T> T getOrElse(String path, T other){
        return ((Optional<T>)get(path, other.getClass())).orElse(other);
    }
    
    public boolean hasValue(String path, Class<?> type){
        return hasValue(get(path), type);
    }
}
