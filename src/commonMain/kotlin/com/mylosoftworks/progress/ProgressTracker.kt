package com.mylosoftworks.progress

abstract class ProgressTracker {
    val activeStack: ArrayDeque<Progress<*>> = ArrayDeque() // Treated as a stack, arraydeque is the closest we have to a real stack.
    val lastActiveOrNull get() = activeStack.lastOrNull()

    abstract fun updateProgress(caller: Progress<*>)

    companion object {
        val defaultGlobal by lazy { createDefaultGlobalTracker() }
    }
}

class CallbackProgressTracker(val callback: Progress<*>.(ArrayDeque<Progress<*>>) -> Unit): ProgressTracker() {
    override fun updateProgress(caller: Progress<*>) {
        callback(caller, activeStack)
    }
}

expect fun ProgressTracker.Companion.createDefaultGlobalTracker(): ProgressTracker