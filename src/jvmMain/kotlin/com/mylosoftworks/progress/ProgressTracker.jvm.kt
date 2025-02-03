package com.mylosoftworks.progress

import org.jline.terminal.TerminalBuilder

class DefaultGlobalTrackerJVM: ProgressTrackerAnsi() {
    override fun getAvailableConsoleWidth(): Int = terminal.width.let { if (it == 0) 0 else it-1 }

    companion object {
        val terminal = TerminalBuilder.builder().jansi(true).build()
    }
}

actual fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker = DefaultGlobalTrackerJVM()