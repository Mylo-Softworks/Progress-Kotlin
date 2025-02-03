package com.mylosoftworks.progress

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

abstract class ProgressTracker {
    val activeStack: ArrayDeque<Progress<*>> = ArrayDeque() // Treated as a stack, arraydeque is the closest we have to a real stack.
    val lastActiveOrNull get() = activeStack.lastOrNull()

    abstract fun updateProgress(caller: Progress<*>)

    companion object {
        val defaultGlobal by lazy { createDefaultGlobalTracker() }

        // https://gist.github.com/ConnerWill/d4b6c776b509add763e17f9f113fd25b

        val escape = "\u001b"

        /**
         * Clear everything on the same line after this.
         */
        val clearAfter = "$escape[0K"

        /**
         * Clear everything on this line.
         */
        val clearLine = "$escape[2K"

        /**
         * Clear everything after this line.
         */
        val clearAllAfter = "$escape[0J"
        /**
         * Go back [count] lines
         */
        fun backLines(count: Int): String = if (count == 0) "" else "$escape[" + count + "F" // Doesn't work for some reason
    }
}

// Note: Printing during a progress bar could overwrite the wrong characters currently.
abstract class ProgressTrackerAnsi : ProgressTracker() {
    var stackCount = 0 // Indicates how far to go back

    abstract fun getAvailableConsoleWidth(): Int

    override fun updateProgress(caller: Progress<*>) {
        queued = {
            val previousStackCount = stackCount
            var newStackCount = 0

            print(backLines(previousStackCount) + clearAllAfter) // Go back and clear what was written

            val printStatement = buildString {
                activeStack.forEachIndexed {i, bar ->
                    bar.format(getAvailableConsoleWidth()).onSuccess {
//                    println("\n" + it.format().also { stackCount += it.lines().count() })
                        append(it.also { newStackCount += it.lines().count() } + "\n")
                    }
                }
            }
            print(printStatement)

            stackCount = newStackCount
        }

        val forceUpdate = activeStack.firstOrNull()?.complete ?: false

        poll(forceUpdate)
        if (forceUpdate) stackCount-- // The current root completed, we don't want to overwrite it on the next iteration.
    }

    var queued: (() -> Unit)? = null
    var timeSource = TimeSource.Monotonic
    var queueEnd: TimeSource.Monotonic.ValueTimeMark? = null

    fun poll(forceUpdate: Boolean) {
        val queueEnd = queueEnd
        if (forceUpdate || queueEnd == null || queueEnd.hasPassedNow()) {
            queued?.let {
                it.invoke()
                this.queueEnd = timeSource.markNow() + 10.milliseconds
            }
        }
    }
}

class CallbackProgressTracker(val callback: Progress<*>.(ArrayDeque<Progress<*>>) -> Unit): ProgressTracker() {
    override fun updateProgress(caller: Progress<*>) {
        callback(caller, activeStack)
    }
}

expect fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker