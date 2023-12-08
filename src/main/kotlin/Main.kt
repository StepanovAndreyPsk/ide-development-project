import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import ui.MainView

fun main() = singleWindowApplication(
    title = "Simple IDE",
    state = WindowState(width = 1280.dp, height = 768.dp)
) {
    MainView()
}