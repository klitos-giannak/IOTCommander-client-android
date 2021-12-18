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
import mobi.duckseason.iotcommander.discover.Device

private val TAG = ControlViewModel::class.java.simpleName
private val COMMANDS_HELP_ENDPOINT = "/commands"

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val selectedDeviceFlow = MutableStateFlow<Device?>(null)
    private val outputFlow = MutableStateFlow("")

    private var requestQueue: RequestQueue = Volley.newRequestQueue(getApplication())

    val controlViewState= selectedDeviceFlow.combine(outputFlow) { device: Device?, output: String ->
        ControlViewState(device, output)
    }

    fun selectDevice(device: Device) {
        selectedDeviceFlow.value = device
        val url = "http://${device.ip}:${device.port}$COMMANDS_HELP_ENDPOINT"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String ->
                outputFlow.value = response
            },
            { volleyError: VolleyError? ->
                Log.e(TAG, "Error trying: url", volleyError)
            }
        )

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

    override fun onCleared() {
        super.onCleared()
        requestQueue.cancelAll { true }
    }
}