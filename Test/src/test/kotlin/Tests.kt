import com.mylosoftworks.progress.Progress
import kotlin.test.Test
import kotlin.test.assertEquals

class Tests {
    @Test
    fun basicIterationTest() {
        for (i in Progress(0..9)) {
            Thread.sleep(100)
        }
    }

    @Test
    fun simpleTest() {
        for (i in Progress.to(200)) {
            Thread.sleep(10)
        }
    }

    @Test
    fun customTracker() {
        var progressString = ""
        for (i in Progress('a'..'j') {
            progressString += "${this.progressFraction} "
        }) {
            // Nothing
        }
        assertEquals("0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 ", progressString)
    }
}