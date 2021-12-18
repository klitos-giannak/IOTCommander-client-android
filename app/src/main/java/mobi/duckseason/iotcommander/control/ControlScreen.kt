package mobi.duckseason.iotcommander.control

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable

@Composable
fun ControlScreen(viewState: ControlViewState) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(viewState.device?.name) },
        content = { Content(viewState.output) }
    )
}

@Composable
private fun AppBar(deviceName: String?) {
    TopAppBar(
        title = {
            deviceName?.let {
                Text(text = it)
            }
        }
    )
}

@Composable fun Content(output: String) {
    Text(text = output)
}