package moe.caa.multilogin.core.data.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moe.caa.multilogin.core.util.YamlReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * config.yml 部分节点阅读程序
 */
@NoArgsConstructor
@Getter
@ToString
public class GeneralConfig {
    private YamlReader reader;
    private long servicesTimeOut = 10000;
    private boolean whitelist = true;
    private String nameAllowedRegular = "^[0-9a-zA-Z_]{1,16}$";

    /**
     * 构建这个阅读程序
     */
    public void reader(File file) throws FileNotFoundException {
        reader = YamlReader.fromInputStream(new FileInputStream(file));
        servicesTimeOut = reader.get("servicesTimeOut", Number.class, servicesTimeOut).longValue();
        whitelist = reader.get("whitelist", Boolean.class, whitelist);
        nameAllowedRegular = reader.get("nameAllowedRegular", String.class, nameAllowedRegular);
    }
}
