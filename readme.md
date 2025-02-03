[![](https://www.jitpack.io/v/Mylo-Softworks/Progress-Kotlin.svg)](https://www.jitpack.io/#Mylo-Softworks/Progress-Kotlin)

# Progress
A Kotlin Multiplatform (Jvm + Js + WasmJs) terminal ([expandable](#renderers)) progress-bar library.

Example with 3 progress bars being rapidly updated:
[![](https://asciinema.org/a/azEgpT4fqx5njOW8ZARQvEjJ7.svg)](https://asciinema.org/a/azEgpT4fqx5njOW8ZARQvEjJ7?autoplay=1)

### Renderers
* Progress uses [org.jline:jansi]() on **JVM** for updating progress bars in terminals, this doesn't work in intellij's run terminal currently, half works in intellij's regular terminal, and works normally on Windows command prompt and most linux/macos terminals.
* **Node.js** supports ansi without any dependencies.
* **BrowserJS** currently uses a basic println-based renderer.
* Custom renderers can be implemented by other developers by extending `ProgressTracker` or with a block for simple/scope-related trackers. Information on implementing custom renderers can be found at [Creating and using custom progress bar renderers](#creating-and-using-custom-progress-bar-renderers)

# Basic usage
## Automatic stack functions
Wrapping iterables.
```kotlin
// Wraps the range (0..9) in a Progress bar. The iteration is now tracked with the bar.

val progress = Progress(0..9)
for (i in progress) {
//          or
for (i in Progress(0..9)) { // Uses the global default ProgressTracker.defaultGlobal
    // Do stuff
    if (breakCond) { // Example: Breaking out safely
        // Get the current progress bar
        progress.exit() // In case val progress = Progress(0..9) was used.
        //    or
        ProgressTracker.defaultGlobal.lastActiveOrNull?.exit() // In case we can't directly access the current progress tracker
        break // In case of break, make sure the progress exits cleanly
    }
}
// Iterable has completed
```
Repeating n times
```kotlin
for (i in Progress.to(9)) { // Similar to Progress(0..<9)
    // Do stuff
}
```

## Manual stack functions
```kotlin
val bar = Progress(0..9,
    // This bar won't enter automatically.
    // setting autoEnter to false also prevents it from activating on init.
    autoEnter=false
)
bar.withEnter { // `it` is the bar itself here, so defining it isn't necessary, however, we defined it as `bar`
    while (bar.hasNext()) {
        val next = bar.next() // Manual iteration over the progress bar iterable
    }
    
    // Alternatively, manually update the progress. Keep in mind that setting `current` does not affect the iterator.
    // Setting `current` updates the bar if autoUpdate is set to true (default)
    bar.current = 5
}
```

# Updating the looks using `BarFormat`
The current (at the time of writing the readme) default `BarFormat`.
```kotlin
// Read the KDoc on BarFormat for documentation on each parameter
val default = BarFormat(fillChars = arrayOf("="), emptyChar = " ", prefix = "[", suffix = "]",
    trailChar = null, title = "", progressFormat = { it.paddedProgress(this) },
    fallBackWidth = 100, maxWidth = null, endInfo = ""
)
```
Using a custom `BarFormat`.
```kotlin
for (i in Progress(0..9, barFormat=BarFormat(title = "Progress:"))) {
    // Do stuff
}
/* Progress bar now looks like this:
Progress: [========= ]  9/10
*/
```

# Creating and using custom progress bar renderers
Progress can be linked to existing UI libraries, in case you don't want to use it from the terminal. All the features from [Basic usage](#basic-usage) are available, however, your custom renderer might decide to ignore `BarFormat` by not using `format(totalBarWidth)` and instead updating a progress bar in the UI.  
There are two ways to implement custom renderers:

1. Using a block
```kotlin
for (i in Progress(0..9) { println(this.format(50)) }) { // Whenever progress is updated, print the new bar with println 
    // Do stuff
}
```
2. Using a `ProgressTracker`
```kotlin
// Define the tracker
class PrintlnProgressTracker: ProgressTracker() {
    override fun updateProgress(caller: Progress<*>) {
        println(caller.format(50))
    }
}
// Use the tracker
for (i in Progress(0..9, tracker=PrintlnProgressTracker())) {
    // Do stuff
}
```
