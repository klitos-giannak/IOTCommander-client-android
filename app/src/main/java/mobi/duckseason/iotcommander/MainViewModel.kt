package mobi.duckseason.iotcommander

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import mobi.duckseason.iotcommander.discover.DiscoverViewState

class MainViewModel: ViewModel() {

    private val _navigation = Channel<NavRoutes>()
    val navigation = _navigation.receiveAsFlow()

    private val _discoverViewState = MutableStateFlow(DiscoverViewState.EMPTY)
    val discoverViewState = _discoverViewState.asStateFlow()

}