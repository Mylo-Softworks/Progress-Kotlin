package com.mylosoftworks.progress

/**
 * The descriptions used for formatting a progress bar.
 *
 * @param fillChars The characters used to display completed segments and segments which are under completion. Use multiple for animating the last char. Example: `arrayOf("0", "1")` for `[1110 ]`
 * @param emptyChar The character used to fill in empty (uncompleted) spaces. Example: `-` for `[====-]`
 * @param prefix The character that is put before the first character of the bar. Example: `[` for `[==== ]`
 * @param suffix The character that is put after the last character of the bar. Example: `]` for `[==== ]`
 * @param trailChar A character to put after the current progress. Example: `>` for `[===> ]`
 * @param title A title to put before the bar. Example: `Bar` for `Bar [==== ]`
 * @param progressFormat A callback to get the current progress, takes [Progress] as the receiver and this [BarFormat] as the parameter. Example: `{ it.paddedProgress(this) }` for `[==== ] 18/20`
 * @param fallBackWidth A width to use in case the terminal's width cannot be determined.
 * @param maxWidth A width to limit the size of the progress bar.
 * @param endInfo A string to add to the end of the bar, similar to title but for the end (Mutable). Example: `example` for `[==== ] 18/20 example`
 */
data class BarFormat(val fillChars: Array<String> = arrayOf("="), val emptyChar: String = " ", val prefix: String = "[", val suffix: String = "]",
    val trailChar: String? = null, val title: String = "", val progressFormat: Progress<*>.(BarFormat) -> String = { it.paddedProgress(this) },
    val fallBackWidth: Int = 100, val maxWidth: Int? = null, var endInfo: String = ""
) {
    init {
        if (fillChars.isEmpty()) error("An empty array was specified for fillChars.")
        fillChars.forEach { if (it.length != 1) error("Fill chars with more than one character are not currently supported.") }
    }

    /**
     * Helper function to create a progress number which is padded to always have the same width.
     */
    fun paddedProgress(progress: Progress<*>, middle: String = "/"): String {
        return " ".repeat(progress.count.toString().length - progress.current.toString().length) + "${progress.current}/${progress.count}"
    }
}