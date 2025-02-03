package com.mylosoftworks.progress

import kotlin.js.JsName
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * @param iter The iterator to wrap.
 * @param count The amount of items in total.
 * @param start The start progress (if any).
 * @param autoUpdate Marks if update() should be called when a value is updated.
 * @param autoEnter Marks if instead of using [enter] and [exit] or [withEnter].
 * @param active Marks if the progress bar should be active initially, set to [autoEnter] by default.
 * @param barFormat Give a custom format for the progress bar's textual representation.
 * @param tracker Assign a custom progress tracker or callback.
 */
class Progress<out T: Any>(val iter: Iterator<T>, val count: Int, start: Int = 0, autoUpdate: Boolean = true,
    val autoEnter: Boolean = true, active: Boolean = autoEnter,
    barFormat: BarFormat = BarFormat(), // Default
    val tracker: ProgressTracker = ProgressTracker.defaultGlobal,
): Iterator<T> by iter { // Proxy the original iterator

    constructor(iter: Iterator<T>, count: Int, start: Int = 0, autoUpdate: Boolean = true,
                autoEnter: Boolean = true, active: Boolean = autoEnter, barFormat: BarFormat = BarFormat(),
                callback: Progress<*>.(ArrayDeque<Progress<*>>) -> Unit)
            : this(iter.iterator(), count, start, autoUpdate, autoEnter, active, barFormat, CallbackProgressTracker(callback))

    constructor(iter: Iterable<T>, count: Int? = null, start: Int = 0, autoUpdate: Boolean = true,
                autoEnter: Boolean = true, active: Boolean = autoEnter, barFormat: BarFormat = BarFormat(),
                tracker: ProgressTracker = ProgressTracker.defaultGlobal)
            : this(iter.iterator(), count ?: iter.count(), start, autoUpdate, autoEnter, active, barFormat, tracker)

    constructor(iter: Iterable<T>, count: Int? = null, start: Int = 0, autoUpdate: Boolean = true,
                autoEnter: Boolean = true, active: Boolean = autoEnter, barFormat: BarFormat = BarFormat(),
                callback: Progress<*>.(ArrayDeque<Progress<*>>) -> Unit)
            : this(iter.iterator(), count ?: iter.count(), start, autoUpdate, autoEnter, active, barFormat, CallbackProgressTracker(callback))

    var barFormat: BarFormat = barFormat // Allows updating bar format
        set(value) {
            field = value
            dirty()
        }

    var active = active
        set(value) {
            if (!field && value) dirty() // Progress bar was activated.
            field = value

            if (value) enter() else exit() // Enter/exit when changing active. Must be after field setter otherwise stackoverflow will occur.
        }

    var autoUpdate = autoUpdate
        set(value) {
            if (value && dirty) update()

            field = value
        }

    /**
     * Indicates whether this progress bar has to be updated. And auto-updates if set to true with [autoUpdate] == `true`.
     */
    var dirty = false
        set(value) {
            field = value
            if (value && autoUpdate) update()
        }

    /**
     * Sets `dirty` to `true`, And auto-updates if [autoUpdate] == `true`.
     */
    @JsName("makeDirty")
    fun dirty() { dirty = true }

    /**
     * The current index we're on. Used to calculate [step].
     */
    var current = start
        set(value) {
            field = value
            dirty()
            if (complete && autoEnter) exit() // Auto exit on completion, IMPORTANT: exit() should be after dirty(), otherwise it'll miss the last callback.
        }

    val progressFraction get() = current / count.toDouble()
    val complete get() = current >= count
    /**
     * The depth of this [Progress]'s bar. Based on the current context.
     */
    var selfDepth: Int? = null

    val activeStack get() = tracker.activeStack

    operator fun iterator() = this

    /**
     * Manually trigger an update of the progress bar.
     * @param ignoreActive If set to true, ignore the [Progress]' active state.
     */
    fun update(ignoreActive: Boolean = false) {
        if (!active && !ignoreActive) return

        tracker.updateProgress(this)
        dirty = false // Clean now, since we updated.
    }

    /**
     * Similar to python's `__enter__()`
     */
    fun enter(ignoreActive: Boolean = false) {
        if (active && !ignoreActive) return
        active = true
        if (this !in activeStack) {
            activeStack.addLast(this)
            selfDepth = activeStack.indexOf(this)
        }
    }

    /**
     * Similar to python's `__exit__()`
     */
    fun exit(ignoreActive: Boolean = false) {
        if (!active && !ignoreActive) return
        active = false
        if (activeStack.lastOrNull() == this) activeStack.removeLastOrNull()
    }

    /**
     * Similar to python's `with x as y:`.
     * @param block A block with this [Progress] as the first parameter.
     */
    fun withEnter(block: (Progress<T>) -> Unit) {
        enter()
        block(this)
        exit()
    }

    fun getCharForInbetween(fraction: Double, progressChars: Array<String>) = progressChars.let { it[(fraction * (it.size-1)).toInt()] }

    // Formatting
    fun format(totalBarWidth: Int): Result<String> {
        val actualBarWidth = (if (totalBarWidth == 0) barFormat.fallBackWidth else totalBarWidth).let {barWidth ->
            barFormat.maxWidth?.let { barWidth.coerceAtMost(it) } ?: barWidth
        }

        val prefix = barFormat.title + " " + barFormat.prefix
        val suffix = barFormat.suffix + " " + barFormat.progressFormat(this, barFormat) + " " + barFormat.endInfo
        val trailChar = barFormat.trailChar
        val trailCharOffset = trailChar?.length ?: 0 // The space required for trailChar

        val progressChars = barFormat.fillChars
        val firstChar = progressChars.last()
        val lastChar = barFormat.emptyChar

        val available = (actualBarWidth - prefix.length - suffix.length) // The available space for the bar itsself
        if (available < 1 + trailCharOffset) return Result.failure(RuntimeException("Not enough available space to fit the progress bar."))
        val availableForPrediction = available - trailCharOffset // The available space for the bar minus the 1 dynamic character

        val storedProgressFraction = progressFraction.coerceIn(0.0, 1.0)
        if (complete) return Result.success("$prefix${firstChar.repeat(available)}$suffix")
        if (storedProgressFraction == 0.0) return Result.success("$prefix${trailChar ?: ""}${lastChar.repeat(availableForPrediction)}$suffix")

        val rawFrac = availableForPrediction * storedProgressFraction
        val left = floor(rawFrac).toInt()
        val right = availableForPrediction - left - 1
        val middleProgress = (rawFrac - left) % 1

        val middleChar = getCharForInbetween(middleProgress, progressChars)

        if (available != (left + right + 1 + trailCharOffset)) error("$left + $right + 1 != $available.")

        val bar = firstChar.repeat(left) + middleChar + (trailChar ?: "") + lastChar.repeat(right)

        return Result.success("$prefix$bar$suffix")
    }
    // End of formatting

    override fun next(): T {
        return iter.next().also { current++ }
    }

    companion object {
        /**
         * Create a [Progress] for a range `(0..<endExclusive)`
         */
        fun to(endExclusive: Int, start: Int = 0, autoUpdate: Boolean = true,
               autoEnter: Boolean = true, active: Boolean = autoEnter,
               barFormat: BarFormat = BarFormat(),
               tracker: ProgressTracker = ProgressTracker.defaultGlobal): Progress<Int> {
            return Progress((start..<endExclusive), endExclusive, start, autoUpdate, autoEnter, active, barFormat, tracker)
        }
        fun to(endExclusive: Int, start: Int = 0, autoUpdate: Boolean = true,
               autoEnter: Boolean = true, active: Boolean = autoEnter,
               barFormat: BarFormat = BarFormat(),
               callback: Progress<*>.(ArrayDeque<Progress<*>>) -> Unit): Progress<Int> {
            return Progress((start..<endExclusive), endExclusive, start, autoUpdate, autoEnter, active, barFormat, callback)
        }
    }

    init {
        if (active) enter(true) // Run enter if active, ignoreActive because it is already set.
        dirty() // Initializes the progress bar at 0 progress. Only applies if autoUpdate is used.
    }
}