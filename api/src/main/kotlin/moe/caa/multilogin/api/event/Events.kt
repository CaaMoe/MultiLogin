package moe.caa.multilogin.api.event

import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

fun <T> createEventHandler(): EventHandler<T> = SimpleEventHandler()

/**
 * 事件分发类
 */
interface EventHandler<T> {

    /**
     * 触发一个事件
     */
    fun callEvent(event: T)

    /**
     * 注册一个事件<br>
     * 如果这个事件已被注册, 则操作失败并且返回上次注册时的 moe.caa.multilogin.api.event.RegisteredListener 实例
     */
    fun registerListener(listener: (event: T) -> Unit): RegisteredListener<T>

    /**
     * 取消注册一个事件
     */
    fun unregisterListener(listener: RegisteredListener<T>) = listener.unregister()

    /**
     * 取消注册一个事件
     */
    fun unregisterListener(listener: (event: T) -> Unit)
}

/**
 * 事件处理器
 */
interface RegisteredListener<T> {

    /**
     * 取消注册这个事件
     */
    fun unregister()

    /**
     * 设置事件的优先级
     */
    fun priority(priority: UInt)
}

internal class SimpleEventHandler<T> : EventHandler<T> {
    private val registeredListeners = CopyOnWriteArraySet<SimpleRegisteredListener>()
    private val reentrantLock = ReentrantReadWriteLock()

    override fun callEvent(event: T) {
        reentrantLock.read {
            registeredListeners.sortedByDescending { it.priority }.forEach {
                it.listener.invoke(event)
            }
        }
    }

    override fun registerListener(listener: (event: T) -> Unit): RegisteredListener<T> {
        reentrantLock.write {
            return registeredListeners.firstOrNull { it.listener == listener }
                ?: SimpleRegisteredListener(listener).also { registeredListeners.add(it) }
        }
    }

    override fun unregisterListener(listener: (event: T) -> Unit) {
        registeredListeners.firstOrNull { it.listener == listener }?.unregister()
    }

    internal inner class SimpleRegisteredListener(
        val listener: (event: T) -> Unit,
        var priority: UInt = 0u
    ) : RegisteredListener<T> {

        override fun unregister() {
            reentrantLock.write {
                registeredListeners.remove(this@SimpleRegisteredListener)
            }
        }

        override fun priority(priority: UInt) {
            this.priority = priority
        }
    }
}

