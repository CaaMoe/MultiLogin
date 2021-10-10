package moe.caa.multilogin.core.loader.libraries;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.FileInputStream;
import java.util.*;

/**
 * 代表一个依赖对象
 */
@Getter(value = AccessLevel.PROTECTED)
@ToString
@Builder(access = AccessLevel.PRIVATE)
public class Library {

    @Getter(value = AccessLevel.PROTECTED)
    private static final List<Library> LIBRARIES;

    static {
        LIBRARIES = Arrays.asList(
                Library.builder()
                        .group("org.slf4j")
                        .name("slf4j-api")
                        .version("1.7.32")
                        .mainClass("org.slf4j.LoggerFactory")
                        .build(),
                Library.builder()
                        .group("org.apache.logging.log4j")
                        .name("log4j-core")
                        .version("2.14.1")
                        .mainClass("org.apache.logging.log4j.core.LoggerContext")
                        .build(),
                Library.builder()
                        .group("org.apache.logging.log4j")
                        .name("log4j-api")
                        .version("2.14.1")
                        .mainClass("org.apache.logging.log4j.Level")
                        .build(),
                Library.builder()
                        .group("com.zaxxer")
                        .name("HikariCP")
                        .version("4.0.3")
                        .mainClass("com.zaxxer.hikari.HikariDataSource")
                        .forwardPackage("com.zaxxer.hikari")
                        .relocate("moe.caa.multilogin.lib.com.zaxxer.hikari")
                        .build(),
                Library.builder()
                        .group("mysql")
                        .name("mysql-connector-java")
                        .version("8.0.25")
                        .mainClass("com.mysql.cj.jdbc.MysqlDataSource")
                        .forwardPackage("com.mysql")
                        .relocate("moe.caa.multilogin.lib.com.mysql")
                        .build(),
                Library.builder()
                        .group("com.h2database")
                        .name("h2")
                        .version("1.4.200")
                        .mainClass("org.h2.jdbcx.JdbcConnectionPool")
                        .build(),
                Library.builder()
                        .group("com.google.code.gson")
                        .name("gson")
                        .version("2.8.8")
                        .mainClass("com.google.gson.Gson")
                        .forwardPackage("com.google.gson")
                        .relocate("moe.caa.multilogin.lib.com.google.gson")
                        .build(),
                Library.builder()
                        .group("org.yaml")
                        .name("snakeyaml")
                        .version("1.29")
                        .mainClass("org.yaml.snakeyaml.DumperOptions")
                        .forwardPackage("org.yaml.snakeyaml")
                        .relocate("moe.caa.multilogin.lib.org.yaml.snakeyaml")
                        .build()
        );
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
    private final String mainClass;

    /**
     * 需要重定向的包名
     */
    private final String forwardPackage;

    /**
     * 重定向后的包名
     */
    private final String relocate;

    /**
     * 生成 Jar 包名称
     * @return Jar 包名称
     */
    public String generateJarName(){
        return name + "-" + version + ".jar";
    }

    /**
     * 需要重定向
     * @return 需要重定向
     */
    public boolean needRelocate(){
        return relocate != null;
    }

    /**
     * 测试重定向后的Class是否已被成功加载
     *
     * @param loader 加载的类加载器
     * @return 是否已被成功加载
     */
    public boolean isLoaded(ClassLoader loader){
        String className = needRelocate() ? mainClass.replace(forwardPackage, relocate) : mainClass;
        System.out.println(className);
        try {
            Class.forName(className, true, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 生成下载链接
     * @return 下载链接
     */
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
