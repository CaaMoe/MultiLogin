package moe.caa.multilogin.velocity.loader

import moe.caa.multilogin.velocity.util.resolves
import java.nio.file.Path

data class Library(
    val group: String,
    val name: String,
    val version: String,
    val md5: String
){
    val localPath = "$group/$name/$version/$name-$version.jar"

    fun getPath(directory: Path): Path = directory.resolves(localPath.split("/"))
}