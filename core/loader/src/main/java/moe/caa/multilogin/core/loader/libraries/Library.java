package moe.caa.multilogin.core.loader.libraries;

import lombok.Getter;

import java.io.FileInputStream;
import java.util.*;

/**
 * 代表一个依赖对象
 */
public class Library {

    @Getter
    private static final List<Library> LIBRARIES;

    static {
        try {
            Properties properties = new Properties();
            //properties.load(Library.class.getResourceAsStream("libraries.properties"));
            properties.load(new FileInputStream("D:\\Desktop\\libraries.properties"));

            List<Library> libraries = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String[] args = entry.getValue().toString().split("\\s+");
                libraries.add(new Library(args[0], args[1], args[2], null, null));
            }
            LIBRARIES = Arrays.asList(libraries.toArray(new Library[0]));
        } catch (Throwable t){
            throw new RuntimeException("AN ERROR OCCURRED IN READING FILE libraries.properties.", t);
        }
    }

    private Library(String group, String name, String version, String rc, String abp) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.rc = rc;
        this.abp = abp;
    }

    public static void main(String[] args) {
        for (Library library : LIBRARIES) {
            System.out.println(library.generateDownloadUrl());
        }
    }

    /**
     * 组
     */
    private final String group;

    /**
     * 名
     */
    private final String name;

    /**
     * 版
     */
    private final String version;

    /**
     * 代表类名称，不含重定向
     */
    private final String rc;

    /**
     * 重定向前追加的包名
     */
    private final String abp;

    /**
     * 生成 Jar 包名称
     * @return Jar 包名称
     */
    public String generateJarName(){
        return name + "-" + version + ".jar";
    }

    public String generateDownloadUrl(){
        // 例子 方便生成URL   https://repo1.maven.org/maven2/com/zaxxer/HikariCP/4.0.2/HikariCP-4.0.2.jar
        StringBuilder sb = new StringBuilder("https://repo1.maven.org/maven2/");
        String[] groupSplit = group.split("\\.");
        for (String s : groupSplit) {
            sb.append(s).append('/');
        }
        sb.append(name).append('/');
        sb.append(version).append('/');
        sb.append(name).append('-');
        sb.append(version).append(".jar");
        return sb.toString();
    }
}
