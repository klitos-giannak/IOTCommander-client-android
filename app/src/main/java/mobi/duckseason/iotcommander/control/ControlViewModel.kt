package mobi.duckseason.iotcommander.control

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import mobi.duckseason.iotcommander.discover.Device

private val TAG = ControlViewModel::class.java.simpleName
private const val COMMANDS_HELP_ENDPOINT = "/commands"
private const val COMMAND_ENDPOINT = "/command"

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val expandedCommands: MutableList<CommandDescription> = mutableListOf()

    private val selectedDeviceFlow = MutableStateFlow<Device?>(null)
    private val supportedCommandsFlow = MutableStateFlow<List<CommandDescription>>(emptyList())
    private val expandedCommandsFlow = MutableStateFlow<List<CommandDescription>>(emptyList())


    private var requestQueue: RequestQueue = Volley.newRequestQueue(getApplication())

    val controlViewState = combine(selectedDeviceFlow, supportedCommandsFlow, expandedCommandsFlow)
    { device, commands, expandedCommands ->
        ControlViewState(device, commands, expandedCommands)
    }

    fun toggleExpanded(commandDescription: CommandDescription) {
        if(expandedCommands.remove(commandDescription).not()) {
            expandedCommands.add(commandDescription)
        }
        expandedCommandsFlow.tryEmit(expandedCommands.toList())
    }

    fun selectDevice(device: Device) {
        selectedDeviceFlow.value = device
        val url = "http://${device.ip}:${device.port}$COMMANDS_HELP_ENDPOINT"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String ->

                val commandsList = parseSupportedCommandsJson(response)

                supportedCommandsFlow.value = commandsList
            },
            { volleyError: VolleyError? ->
                Log.e(TAG, "Error trying: url", volleyError)
                //TODO show network related error?
            }
        )

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

    private fun parseSupportedCommandsJson(response: String) =
        Json.decodeFromString(JsonObject.serializer(), response)
            .map { command ->
                val params = (command.value as JsonObject)
                    .map { param ->
                        val paramType = (param.value as? JsonPrimitive).let {
                            if (it == null || it.isString.not()) {
                                throw SerializationException()
                            } else {
                                ParameterType.fromInternalValue(it.content)
                            }
                        }

                        if (paramType == null) {
                            throw SerializationException()
                        } else {
                            ParameterDescription(param.key, paramType)
                        }
                    }
                CommandDescription(command.key, params)
            }

    fun onOutGoingCommand(outGoingCommand: OutGoingCommand) {
        if (validateParams(outGoingCommand.paramsValues)) {
            sendCommand(outGoingCommand)
        } else {
            //TODO show validation error?
        }
    }

    private fun sendCommand(outGoingCommand: OutGoingCommand) {
        selectedDeviceFlow.value?.let { device ->
            val url = "http://${device.ip}:${device.port}$COMMAND_ENDPOINT/${outGoingCommand.command.name}"
                .let {
                    if (outGoingCommand.paramsValues.isEmpty()) {
                        Log.d(TAG, "empty params")
                        it
                    } else {
                        Log.d(TAG, "found ${outGoingCommand.paramsValues.size} params")
                        StringBuilder(it)
                            .apply {
                                append("?")

                                //add all parameters to the url
                                outGoingCommand.paramsValues.forEach { (param, value) ->
                                    append("&${param.name}=$value")
                                }


                            }
                            //now remove the ampersand before the first parameter
                            .replace(Regex.fromLiteral("?&"), "?")
                    }
                }

            Log.d(TAG, "Request: $url")

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response: String ->
                    Log.d(TAG, "Response: $response")
                },
                { volleyError: VolleyError? ->
                    Log.e(TAG, "Error trying: url", volleyError)
                    //TODO show network related error?
                }
            )

            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest)
        }
//            ?: TODO show unexpected error?

    }

    private fun validateParams(paramsValues: Map<ParameterDescription, Any?>): Boolean {
        // filter non validated values. We expect the size to be 0
        return paramsValues
            .filter { entry ->
                val isValid = entry.value?.let {
                    entry.key.type.validate(it)
                } ?: false

                !isValid
            }
            .isEmpty()
    }

    override fun onCleared() {
        super.onCleared()
        requestQueue.cancelAll { true }
    }
}