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

fun isNodeJs() = js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null") as Boolean
fun windowWidthNode(default: Int) = (js("process.stdout.columns") ?: default) as Int
actual fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker = if (isNodeJs()) DefaultGlobalTrackerNodeJS() else DefaultGlobalTrackerJS()