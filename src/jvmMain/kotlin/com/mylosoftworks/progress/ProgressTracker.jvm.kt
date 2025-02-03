package com.mylosoftworks.progress

import org.jline.terminal.TerminalBuilder
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

// Note: Printing during a progress bar could overwrite the wrong characters currently.
class DefaultGlobalTrackerJVM: ProgressTracker() {
    var stackCount = 0 // Indicates how far to go back

    override fun updateProgress(caller: Progress<*>) {
        queued = {
            val previousStackCount = stackCount
            var newStackCount = 0

            print(backLines(previousStackCount) + clearAllAfter) // Go back and clear what was written

            val printStatement = buildString {
                activeStack.forEachIndexed {i, bar ->
                    bar.format(getAvailableConsoleWidth()).onSuccess {
//                    println("\n" + it.format().also { stackCount += it.lines().count() })
                        append(it.format().also { newStackCount += it.lines().count() } + "\n")
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

    // Alternatively, use jansi https://github.com/fusesource/jansi

    companion object {
        val terminal = TerminalBuilder.builder().jansi(true).build()

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

        fun getAvailableConsoleWidth() = terminal.width.let { if (it == 0) 0 else it-1 } // -1 so console will contine to work even when resized
//        fun getAvailableConsoleWidth(fallback: Int = 20) = fallback
    }
}

actual fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker = DefaultGlobalTrackerJVM()