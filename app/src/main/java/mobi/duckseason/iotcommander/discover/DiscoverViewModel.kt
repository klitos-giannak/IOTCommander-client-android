package mobi.duckseason.iotcommander.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import mobi.duckseason.iotcommander.discover.DiscoverViewState.LoadingState

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceDiscoverer = DeviceDiscoverer(application)
        .apply {
            viewModelScope.launch {
                invokeSearch()
            }
        }

    val discoverViewState: Flow<DiscoverViewState> =
        with(deviceDiscoverer) {
            combine(
                devicesFlow,
                searching,
                networkError
            ) { devices: Set<Device>, loading: Boolean, networkError: Boolean ->
                if (networkError) {
                    DiscoverViewState.NETWORK_ERROR
                } else {
                    DiscoverViewState(
                        devices.toList(),
                        if (loading) LoadingState.LOADING else LoadingState.NOT_LOADING
                    )
                }
            }
        }

    fun searchForDevices() {
        viewModelScope.launch {
            deviceDiscoverer.invokeSearch()
        }
    }
}