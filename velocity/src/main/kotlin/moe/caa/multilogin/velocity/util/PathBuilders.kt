package moe.caa.multilogin.velocity.util

import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun buildPath(directory: Path, builderAction: PathBuilder.() -> Unit): Path {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val builder = PathBuilder()
    builder.builderAction()
    return builder.build(directory)
}


class PathBuilder @PublishedApi internal constructor(
    private val list: MutableList<String> = ArrayList(),
) {
    fun resolve(other: String) {
        list.add(other)
    }

    fun build(directory: Path): Path {
        var path = directory
        list.forEach {
            path = path.resolve(it)
        }
        return path
    }
}