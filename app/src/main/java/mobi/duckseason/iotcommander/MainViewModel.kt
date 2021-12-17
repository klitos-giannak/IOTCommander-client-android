package mobi.duckseason.iotcommander

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import mobi.duckseason.iotcommander.discover.Device
import mobi.duckseason.iotcommander.discover.DeviceDiscoverer
import mobi.duckseason.iotcommander.discover.DiscoverViewState

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _navigation = Channel<NavRoutes>()
    val navigation = _navigation.receiveAsFlow()

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

    fun selectDevice(device: Device) {
        // TODO
    }

    fun onBackNavigation() {
        _navigation.trySend(NavRoutes.BACK)
    }
}