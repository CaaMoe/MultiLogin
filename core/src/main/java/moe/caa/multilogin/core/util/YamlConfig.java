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
    
    private Class<?> getType(Object obj){
        return obj == null ?  null : obj.getClass();
    }
    
    public Optional<?> get(String path){
        return Optional.ofNullable(contents.get(path));
    }
    
    public Optional<String> getString(String path){
        Optional<?> value = get(path);
        if(value.isPresent() && value.get() instanceof String){
            return (Optional<String>)value;
        }
        return Optional.empty();
    }
    
    public Optional<String> getString(String path, String defaultValue){
        Optional<String> value = getString(path);
        return value.isPresent() ? value : Optional.ofNullable(defaultValue);
        
    }
    
    public boolean hasValue(String path, Class<?> type){
        if(contents.containsKey(path)){
            if(type == getType(contents.get(path))){
                return true;
            }
        }
        return false;
    }
}
