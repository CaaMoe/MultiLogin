package moe.caa.multilogin.velocity.util

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class FormattedDaemonThreadFactory(private val formatName: String) : ThreadFactory {
    private val threadNumber = AtomicInteger(0)

    override fun newThread(r: Runnable) = Thread(r).apply {
        isDaemon = true
        name = formatName.format(threadNumber.getAndIncrement())
    }
}