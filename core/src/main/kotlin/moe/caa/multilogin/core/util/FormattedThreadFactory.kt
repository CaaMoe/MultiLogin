package moe.caa.multilogin.core.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class FormattedThreadFactory(
    private val format: String,
    private val factory: ThreadFactory
) : ThreadFactory {
    private val atomicInt = AtomicInteger()

    override fun newThread(r: Runnable): Thread = factory.newThread(r).apply {
        name = String.format(format, atomicInt.getAndIncrement())
    }
}