package mobi.duckseason.iotcommander.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceDiscoverer = DeviceDiscoverer(application)
        .apply {
            invokeSearch()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val discoverViewState: Flow<DiscoverViewState> =
        with(deviceDiscoverer) {
            devicesFlow.combine(searching) { devices: Set<Device>, loading: Boolean ->
                DiscoverViewState(devices.toList(), loading)
            }
        }

    fun searchForDevices() {
        deviceDiscoverer.invokeSearch()
    }
}