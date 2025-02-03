package com.mylosoftworks.progress

class DefaultGlobalTrackerJS: ProgressTracker() {
    override fun updateProgress(caller: Progress<*>) {
        activeStack.forEachIndexed {i, it ->
            println(it.format(25))
        }
    }
}

actual fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker = DefaultGlobalTrackerJS()