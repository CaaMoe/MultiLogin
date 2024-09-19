package moe.caa.multilogin.velocity.loader.library

import moe.caa.multilogin.velocity.util.md5Digest
import java.io.File

data class Library(
    val group: String,
    val name: String,
    val version: String,
    val md5: ByteArray
) {
    private val fileName = "$name-$version.jar"
    private val url = "${group.replace('.', '/')}/$name/$version/$fileName"

    fun getFile(folder: File): File {
        return File(folder, url)
    }


    fun md5Check(libraryFile: File): Boolean{
        return libraryFile.md5Digest().contentEquals(md5)
    }
}
