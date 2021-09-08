package moe.caa.multilogin.core.library;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 代表一个 Maven 库文件信息
 */
@Data
@Builder(access = AccessLevel.PRIVATE)
public class Library {
    private static final String MAVEN_ALIYUN = "https://maven.aliyun.com/repository/public/";

    @Getter(value = AccessLevel.PROTECTED)
    private static List<Library> libraries;

    private final String group;
    private final String name;
    private final String version;
    private final String mainClass;
    private String downloadUrl;
    private String jarName;

    /**
     * 生成依赖的本体文件名称
     * @return 依赖的本体文件名称
     */
    protected String getJarName(){
        if(jarName == null) jarName = name + '-' + version + ".jar";
        return jarName;
    }

    /**
     * 生成依赖的本体文件的下载地址
     * @return 依赖本体的下载地址
     */
    protected String getDownloadUrl(){
        if(downloadUrl == null){
            StringBuilder sb = new StringBuilder(MAVEN_ALIYUN);
            String[] packetSplit = group.split("\\.");
            for (String get : packetSplit) {
                sb.append(get);
                sb.append('/');
            }
            sb.append(name).append('/').append(version).append('/');
            sb.append(name).append('-').append(version).append(".jar");
            downloadUrl = sb.toString();
        }
        return downloadUrl;
    }

    /**
     * 根据代表类判断依赖是否已被加载
     * @return 依赖是否已被加载
     */
    protected boolean isLoaded(){
        return ReflectUtil.isExistsClass(mainClass);
    }

    static {
        libraries = Arrays.asList(
                Library.builder().group("org.slf4j").name("slf4j-api").version("1.7.31").mainClass("org.slf4j.LoggerFactory").build(),
                Library.builder().group("com.zaxxer").name("HikariCP").version("4.0.3").mainClass("com.zaxxer.hikari.HikariDataSource").build(),
                Library.builder().group("mysql").name("mysql-connector-java").version("8.0.25").mainClass("com.mysql.cj.jdbc.Driver").build(),
                Library.builder().group("com.h2database").name("h2").version("1.4.200").mainClass("org.h2.jdbcx.JdbcConnectionPool").build(),
                Library.builder().group("com.google.code.gson").name("gson").version("2.8.7").mainClass("com.google.gson.Gson").build(),
                Library.builder().group("org.apache.logging.log4j").name("log4j-api").version("2.14.1").mainClass("org.apache.logging.log4j.Level").build(),
                Library.builder().group("org.apache.logging.log4j").name("log4j-core").version("2.14.1").mainClass("org.apache.logging.log4j.core.LoggerContext").build()
        );
    }
}
