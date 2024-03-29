package mobi.duckseason.iotcommander.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iotcommander.R
import mobi.duckseason.iotcommander.discover.DiscoverViewState.LoadingState
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

private val SPACING_SMALL = 10.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_HUGE = 32.dp
private val SPACING_ENORMOUS = 64.dp

@Composable
fun DiscoverScreen(
    discoverViewState: DiscoverViewState,
    onRefreshRequested: () -> Unit,
    onSelectItem: (device:Device) -> Unit
) {

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(onRefreshRequested, discoverViewState.loadingState) },
        content = { Content(discoverViewState, onSelectItem) }
    )
}

@Composable
private fun AppBar(onRefreshRequested: () -> Unit, loadingState: LoadingState) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.discover_title))
        },
        actions = {
            IconButton(
                onClick = onRefreshRequested,
                enabled = loadingState != LoadingState.LOADING
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                )
            }
        }
    )
}

@Composable
private fun Content(discoverViewState: DiscoverViewState, onSelectItem: (device: Device) -> Unit) {
    if (discoverViewState.loadingState == LoadingState.ERROR) {
        DiscoverEmptyState(stringResource(id = R.string.discover_network_error_message))
    } else if (discoverViewState.devices.isEmpty() && discoverViewState.loadingState == LoadingState.NOT_LOADING) {
        DiscoverEmptyState(stringResource(id = R.string.discover_empty_state_message))
    }

    Column {
        LazyColumn(
            contentPadding = PaddingValues(SPACING_SMALL),
            verticalArrangement = Arrangement.spacedBy(SPACING_SMALL)
        ) {
            items(discoverViewState.devices) { item: Device ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colors.surface,
                            shape = RoundedCornerShape(SPACING_SMALL)
                        )
                        .padding(horizontal = SPACING_SMALL * 2, vertical = SPACING_SMALL)
                        .clickable { onSelectItem(item) }
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

        if (discoverViewState.loadingState == LoadingState.LOADING) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(SPACING_MEDIUM)
                )
            }
        }

    }
}

@Composable
private fun DiscoverEmptyState(title: String) {
    Column(modifier = Modifier.padding(SPACING_ENORMOUS)) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SPACING_HUGE),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.discover_empty_state_description),
            textAlign = TextAlign.Center
        )
    }
}

private val previewDevices = listOf(
    Device("Door Handler", "192.168.1.145"),
    Device("Thermostat", "192.168.1.190"),
    Device("Kitchen LED-strip", "192.168.1.101"),
    Device("Lounge lights", "192.168.1.120"),
    Device("Security Camera Front", "192.168.1.121"),
    Device("Security Camera Back", "192.168.1.122"),
)

@Composable
@Preview
private fun DarkPreview() {
    IOTCommanderTheme(darkTheme = true) {
        DiscoverScreen(DiscoverViewState(devices = previewDevices, LoadingState.NOT_LOADING), {}, {})
    }
}

@Composable
@Preview
private fun LightPreview() {
    IOTCommanderTheme(darkTheme = false) {
        DiscoverScreen(DiscoverViewState(devices = previewDevices, LoadingState.NOT_LOADING), {}, {})
    }
}

@Composable
@Preview
private fun EmptyStatePreview() {
    IOTCommanderTheme(darkTheme = true) {
        DiscoverScreen(DiscoverViewState(devices = emptyList(), LoadingState.NOT_LOADING), {}, {})
    }
}
