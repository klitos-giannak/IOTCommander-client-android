package mobi.duckseason.iotcommander.control

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import iotcommander.R
import mobi.duckseason.iotcommander.discover.Device
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

private val SPACING_SMALL = 10.dp
private val SPACING_HUGE = 32.dp
private val SPACING_ENORMOUS = 64.dp

private val CORNER_RADIOUS_SMALL = 4.dp
private val CORNER_RADIOUS_LARGE = 10.dp

@Composable
fun ControlScreen(
    viewState: ControlViewState,
    onToggleExpanded: (commandDescription: CommandDescription) -> Unit,
    onSendButtonPressed: (commandDescription: CommandDescription) -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(viewState.device?.name) },
        content = {
            Content(
                viewState,
                onToggleExpanded,
                onSendButtonPressed
            )
        }
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

@Composable
private fun Content(
    viewState: ControlViewState,
    onCommandToggleExpanded: (commandDescription: CommandDescription) -> Unit,
    onSendButtonPressed: (commandDescription: CommandDescription) -> Unit
) {
    if (viewState.commands.isEmpty()) {
        ControlEmptyState()
    }

    LazyColumn(
        contentPadding = PaddingValues(SPACING_SMALL),
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL)
    ) {
        items(viewState.commands) { item ->
            Command(
                commandDescription = item,
                isExpanded = viewState.expandedCommands.contains(item),
                onCommandToggleExpanded,
                onSendButtonPressed
            )
        }
    }
}

@Composable
private fun Command(
    commandDescription: CommandDescription,
    isExpanded: Boolean,
    onToggleExpanded: (commandDescription: CommandDescription) -> Unit,
    onSendButtonPressed: (commandDescription: CommandDescription) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(CORNER_RADIOUS_LARGE)
                )
                .padding(horizontal = SPACING_SMALL * 2, vertical = SPACING_SMALL)
                .clickable {
                    if (commandDescription.params
                            .isEmpty()
                            .not()
                    ) {
                        onToggleExpanded(commandDescription)
                    }
                }
        ) {
            Text(
                text = commandDescription.name,
                modifier = Modifier
                    .padding(0.dp, SPACING_SMALL, SPACING_SMALL, SPACING_SMALL)
                    .weight(1f)
            )

            if (commandDescription.params.isEmpty()) {
                SendButton(onSendButtonPressed, commandDescription)
            } else {
                Icon(
                    imageVector = with(Icons.Default) { if (isExpanded) KeyboardArrowUp else KeyboardArrowDown },
                    contentDescription = null,
                )
            }
        }

        if (isExpanded) {
            ParamsBox(commandDescription, onSendButtonPressed)
        }

    }
}

@Composable
private fun ParamsBox(
    commandDescription: CommandDescription,
    onSendButtonPressed: (commandDescription: CommandDescription) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SPACING_SMALL, 0.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(CORNER_RADIOUS_SMALL)
    ) {
        Column {
            commandDescription.params.forEach { param ->
                Text(
                    text = param.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SPACING_SMALL * 2, vertical = SPACING_SMALL)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SPACING_SMALL),
                contentAlignment = Alignment.CenterEnd
            ) {
                SendButton(
                    onSendButtonPressed = onSendButtonPressed,
                    commandDescription = commandDescription
                )
            }
        }
    }
}

@Composable
private fun SendButton(
    onSendButtonPressed: (commandDescription: CommandDescription) -> Unit,
    commandDescription: CommandDescription
) {
    Button(
        onClick = { onSendButtonPressed(commandDescription) }
    ) {
        Text(text = stringResource(id = R.string.control_button_send))
    }
}

@Composable
private fun ControlEmptyState() {
    Column(modifier = Modifier.padding(SPACING_ENORMOUS)) {
        Text(
            text = stringResource(id = R.string.control_empty_state_message),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SPACING_HUGE),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.control_empty_state_description),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
private fun DarkPreview() {
    val command2 = CommandDescription(
        "test-Collapsed",
        listOf(
            ParameterDescription("myString", ParameterType.TEXT),
        )
    )
    val command3 = CommandDescription(
        "testAll-Expanded",
        listOf(
            ParameterDescription("myString", ParameterType.TEXT),
            ParameterDescription("myInt", ParameterType.INT),
            ParameterDescription("myFloat", ParameterType.FLOAT),
            ParameterDescription("myBoolean", ParameterType.BOOLEAN),
        )
    )
    val viewState = ControlViewState(
        device = Device(name = "MyDevice", ""),
        commands = listOf(
            CommandDescription("testNoParams", emptyList()),
            command2,
            command3
        ),
        listOf(command2, command3)
    )

    IOTCommanderTheme(darkTheme = true) {
        ControlScreen(viewState, {}, {})
    }
}

@Composable
@Preview
private fun EmptyStatePreview() {
    IOTCommanderTheme(darkTheme = true) {
        ControlScreen(
            ControlViewState(
                device = Device(name = "MyDevice", ""),
                commands = emptyList()
            ),
            {}, {}
        )
    }
}