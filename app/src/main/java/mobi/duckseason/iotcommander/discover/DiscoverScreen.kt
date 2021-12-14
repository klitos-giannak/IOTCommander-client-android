package mobi.duckseason.iotcommander.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iotcommander.R
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

private val SPACING = 10.dp

@Composable
fun DiscoverScreen(discoverViewState: DiscoverViewState) {

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar() },
        content = { Content(discoverViewState.devices) }
    )
}

@Composable
private fun AppBar() {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.discover_title))
        }
    )
}

@Composable
private fun Content(devices: List<Device>) {
    LazyColumn(
        contentPadding = PaddingValues(SPACING),
        verticalArrangement = Arrangement.spacedBy(SPACING)
    ) {
        items(devices) { item: Device ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(SPACING)
                    )
                    .padding(horizontal = SPACING * 2, vertical = SPACING)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.ip,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    modifier = Modifier.alpha(0.4f)
                )
            }
        }
    }
}

@Composable
@Preview
private fun DarkPreview() {
    val devices = listOf(
        Device("Door Handler", "192.168.1.145"),
        Device("Thermostat", "192.168.1.190")
    )

    IOTCommanderTheme(darkTheme = true) {
        DiscoverScreen(DiscoverViewState(devices = devices))
    }
}

@Composable
@Preview
private fun LightPreview() {
    val devices = listOf(
        Device("Door Handler", "192.168.1.145"),
        Device("Thermostat", "192.168.1.190")
    )

    IOTCommanderTheme(darkTheme = false) {
        DiscoverScreen(DiscoverViewState(devices = devices))
    }
}
