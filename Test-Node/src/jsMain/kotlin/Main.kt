import com.mylosoftworks.progress.BarFormat
import com.mylosoftworks.progress.Progress

fun main() {
    for (i in Progress.to(5, barFormat = BarFormat(title = "Progress 1:"))) {
        for (j in Progress.to(10, barFormat = BarFormat(title = "Progress 2:"))) {
            for (k in Progress.to(20, barFormat = BarFormat(title = "Progress 3:"))) {
                for (l in (0..30000000)) {
                    val result = 2 * 2
                }
            }
        }
    }
    println("Done!")
}