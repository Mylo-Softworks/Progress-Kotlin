import com.mylosoftworks.progress.BarFormat
import com.mylosoftworks.progress.Progress

fun main() {
    for (i in Progress.to(20, barFormat = BarFormat(title = "Progress 1:"))) {
        for (j in Progress.to(20, barFormat = BarFormat(title = "Progress 2:"))) {
            for (k in Progress.to(20, barFormat = BarFormat(title = "Progress 3:"))) {
                Thread.sleep(5)
            }
        }
    }
}