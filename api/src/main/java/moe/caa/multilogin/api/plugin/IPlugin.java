package moe.caa.multilogin.api.plugin;

import java.io.File;

public interface IPlugin {

    /**
     * 获得配置和数据文件路径
     *
     * @return 配置和数据文件路径
     */
    File getDataFolder();

    /**
     * 获得临时目录文件夹
     *
     * @return 临时目录文件夹
     */
    File getTempFolder();

    /**
     * 获得服务器对象
     *
     * @return 服务器对象
     */
    IServer getRunServer();

    String getVersion();
}
