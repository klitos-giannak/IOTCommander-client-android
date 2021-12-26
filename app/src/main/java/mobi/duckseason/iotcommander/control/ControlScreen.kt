package mobi.duckseason.iotcommander.control

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import iotcommander.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mobi.duckseason.iotcommander.discover.Device
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

private val SPACING_TINY = 2.dp
private val SPACING_SMALL = 10.dp
private val SPACING_HUGE = 32.dp
private val SPACING_ENORMOUS = 64.dp

private val CORNER_RADIUS_SMALL = 4.dp
private val CORNER_RADIUS_LARGE = 10.dp

private const val PARAM_NAME_WEIGHT = 0.4f
private const val PARAM_ENTRY_WEIGHT = 0.6f

@Composable
fun ControlScreen(
    viewState: ControlViewState,
    onToggleExpanded: (commandDescription: CommandDescription) -> Unit,
    onOutGoingCommand: (outGoingCommand: OutGoingCommand) -> Unit,
    scope: CoroutineScope
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(viewState.device?.name) },
        content = {
            Content(
                viewState,
                onToggleExpanded,
                onOutGoingCommand,
                scope
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
    onOutGoingCommand: (outGoingCommand: OutGoingCommand) -> Unit,
    scope: CoroutineScope
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
                onOutGoingCommand,
                scope
            )
        }
    }
}

@Composable
private fun Command(
    commandDescription: CommandDescription,
    isExpanded: Boolean,
    onToggleExpanded: (commandDescription: CommandDescription) -> Unit,
    onOutGoingCommand: (outGoingCommand: OutGoingCommand) -> Unit,
    scope: CoroutineScope
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(CORNER_RADIUS_LARGE)
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
                SendButton { onOutGoingCommand(OutGoingCommand(commandDescription)) }
            } else {
                Icon(
                    imageVector = with(Icons.Default) { if (isExpanded) KeyboardArrowUp else KeyboardArrowDown },
                    contentDescription = null,
                )
            }
        }

        if (isExpanded) {
            ParamsBox(commandDescription, onOutGoingCommand, scope)
        }

    }
}

@Composable
private fun ParamsBox(
    commandDescription: CommandDescription,
    onOutGoingCommand: (outGoingCommand: OutGoingCommand) -> Unit,
    scope: CoroutineScope
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SPACING_SMALL, vertical = 0.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(CORNER_RADIUS_SMALL)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(SPACING_SMALL)
        ) {
            val paramsStateMap = mutableMapOf<ParameterDescription, MutableState<*>>()
            commandDescription.params.forEach { param ->
                Row(
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = SPACING_SMALL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = param.name,
                        modifier = Modifier.weight(PARAM_NAME_WEIGHT)
                    )

                    val valueEntryModifier = Modifier.weight(PARAM_ENTRY_WEIGHT)
                    when (param.type) {
                        ParameterType.BOOLEAN -> {
                            val state: MutableState<Boolean?> = remember { mutableStateOf(null) }
                            paramsStateMap[param] = state
                            ParamsEntryBoolean(
                                modifier = valueEntryModifier,
                                state = state
                            )
                        }
                        ParameterType.INT -> {
                            val state: MutableState<Int?> = remember { mutableStateOf(null) }
                            paramsStateMap[param] = state
                            ParamsEntryInt(
                                modifier = valueEntryModifier,
                                state = state,
                                scope = scope
                            )
                        }
                        ParameterType.FLOAT -> {
                            val state: MutableState<Float?> = remember { mutableStateOf(null) }
                            paramsStateMap[param] = state
                            ParamsEntryFloat(
                                modifier = valueEntryModifier,
                                state = state,
                                scope = scope
                            )
                        }
                        ParameterType.TEXT -> {
                            val state: MutableState<String> = remember { mutableStateOf("") }
                            paramsStateMap[param] = state
                            ParamsEntryText(
                                modifier = valueEntryModifier,
                                state = state,
                                scope = scope
                            )
                        }
                    }
                }

            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                SendButton(
                    onSendButtonPressed = {
                        val paramsValues = paramsStateMap.mapValues { entry -> entry.value.value }
                        val outGoingCommand = OutGoingCommand(
                            command = commandDescription,
                            paramsValues = paramsValues
                        )
                        onOutGoingCommand(outGoingCommand)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParamsEntryText(
    modifier: Modifier,
    state: MutableState<String>,
    scope: CoroutineScope
) {

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    TextField(
        value = state.value.toString(),
        onValueChange = { newState: String -> state.value = newState },
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent {
                if (it.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            autoCorrect = false,
            imeAction = ImeAction.Next
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParamsEntryInt(
    modifier: Modifier,
    state: MutableState<Int?>,
    scope: CoroutineScope
) {

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    TextField(
        value = state.value?.toString() ?: "",
        onValueChange = { newValue: String ->
            state.value = newValue
                .filter { it.isDigit() }
                .toIntOrNull()
        },
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent {
                if (it.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParamsEntryFloat(
    modifier: Modifier,
    state: MutableState<Float?>,
    scope: CoroutineScope
) {
    // we need a second state because converting from string to float and then string again causes issues
    val stringState = remember { mutableStateOf("") }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    TextField(
        value = stringState.value ?: "",
        onValueChange = { newValue: String ->
            val asFloat = newValue.toFloatOrNull()
            if (asFloat != null) {
                stringState.value = newValue
                state.value = asFloat
            }
        },
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent {
                if (it.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun ParamsEntryBoolean(modifier: Modifier, state: MutableState<Boolean?>) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = state.value == true,
            onClick = { state.value = true }
        )
        Text(
            text = stringResource(R.string.control_option_true),
            style = MaterialTheme.typography.body1.merge(),
            modifier = Modifier.padding(horizontal = SPACING_TINY)
        )
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = state.value == false,
            onClick = { state.value = false }
        )
        Text(
            text = stringResource(R.string.control_option_false),
            style = MaterialTheme.typography.body1.merge(),
            modifier = Modifier.padding(horizontal = SPACING_TINY)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SendButton(
    onSendButtonPressed: () -> Unit
) {
    Button(
        onClick = onSendButtonPressed
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

private val previewCommand2 = CommandDescription(
    "test-Collapsed",
    listOf(
        ParameterDescription("myString", ParameterType.TEXT),
    )
)

private val previewCommand3 = CommandDescription(
    "testAll-Expanded",
    listOf(
        ParameterDescription("myString", ParameterType.TEXT),
        ParameterDescription("myInt", ParameterType.INT),
        ParameterDescription("myFloat", ParameterType.FLOAT),
        ParameterDescription("myBoolean", ParameterType.BOOLEAN),
    )
)

private val previewViewState = ControlViewState(
    device = Device(name = "MyDevice", ""),
    commands = listOf(
        CommandDescription("testNoParams", emptyList()),
        previewCommand2,
        previewCommand3
    ),
    listOf(previewCommand3)
)

@Composable
@Preview
private fun DarkPreview() {
    IOTCommanderTheme(darkTheme = true) {
        ControlScreen(previewViewState, {}, {}, rememberCoroutineScope())
    }
}

@Composable
@Preview
private fun LightPreview() {
    IOTCommanderTheme(darkTheme = false) {
        ControlScreen(previewViewState, {}, {}, rememberCoroutineScope())
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
            {},
            {},
            rememberCoroutineScope()
        )
    }
}