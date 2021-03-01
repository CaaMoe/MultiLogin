package moe.caa.multilogin.core.impl;

/**
 * 可被core识别的配置文件对象
 */
public interface IConfiguration {

    /**
     * 通过path获得一个子configuration对象
     *
     * @param path path
     * @return 他的子对象
     */
    IConfiguration getConfigurationSection(String path);

    /**
     * 通过path获得一个String
     *
     * @param path path
     * @return 一个String
     */
    String getString(String path);

    /**
     * 通过path获得一个String
     *
     * @param path path
     * @param def  默认值
     * @return 一个String，如果值为空，则返回默认值
     */
    String getString(String path, String def);

    /**
     * 通过path获得一个Boolean
     *
     * @param path path
     * @return 一个Boolean
     */
    boolean getBoolean(String path);

    /**
     * 通过path获得一个Boolean
     *
     * @param path path
     * @param def  默认值
     * @return 一个Boolean，如果值为空，则返回默认值
     */
    boolean getBoolean(String path, boolean def);

    /**
     * 通过path获得一个Long
     *
     * @param path path
     * @return 一个Long
     */
    long getLong(String path);

    /**
     * 通过path获得一个Long
     *
     * @param path path
     * @param def  默认值
     * @return 一个Long，如果值为空，则返回默认值
     */
    long getLong(String path, long def);

    /**
     * 设置某个path的值
     *
     * @param path  path
     * @param value 值
     */
    void set(String path, Object value);

    /**
     * 获得当前节点的所有子节点
     *
     * @param b 是否包含所有子节点的子节点
     * @return 节点列表
     */
    String[] getKeys(boolean b);

    Object getVanConfiguration();
}
