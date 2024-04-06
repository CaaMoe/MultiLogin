package moe.caa.multilogin.core.command.handles

import moe.caa.multilogin.core.resource.builddata.getBuildData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.incendo.cloud.execution.CommandExecutionHandler

val versionHandle = CommandExecutionHandler<Audience> {
    it.sender().sendMessage(Component.text("Version: ${getBuildData("version")}"))
    it.sender().sendMessage(Component.text("Build Type: ${getBuildData("build_type")}"))
    it.sender().sendMessage(Component.text("Build By: ${getBuildData("build_by")}"))
    it.sender().sendMessage(Component.text("Build Time: ${getBuildData("build_timestamp")}"))
    it.sender().sendMessage(Component.text("Build Revision: ${getBuildData("build_revision")}"))
}