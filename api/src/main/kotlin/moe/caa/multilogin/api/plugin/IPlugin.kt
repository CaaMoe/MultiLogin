package moe.caa.multilogin.api.plugin

import net.kyori.adventure.audience.Audience
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
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

    fun generateCommandManager(
        executionCoordinator: ExecutionCoordinator<Audience>
    ): CommandManager<Audience>

    fun checkEnvironment(): EnvironmentalCheckResult
}
