package com.mylosoftworks.progress

class DefaultGlobalTrackerJS: ProgressTracker() {
    override fun updateProgress(caller: Progress<*>) {
        activeStack.forEachIndexed {i, it ->
            println(it.format(25))
        }
    }
}

class DefaultGlobalTrackerNodeJS: ProgressTrackerAnsi() {
    override fun getAvailableConsoleWidth(): Int = windowWidthNode(1) - 1
}

fun isNodeJs(): Boolean = js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null")
fun windowWidthNode(default: Int): Int = js("process.stdout.columns") ?: default
actual fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker = if (isNodeJs()) DefaultGlobalTrackerNodeJS() else DefaultGlobalTrackerJS()