package moe.caa.multilogin.api.plugin

import org.jetbrains.annotations.ApiStatus
import java.io.File

/**
 * 表示一个插件
 */
@ApiStatus.Internal
interface IPlugin {
    /**
     * 返回插件数据文件路径
     * @return 插件数据文件路径
     */
    val dataFolder: File
}
