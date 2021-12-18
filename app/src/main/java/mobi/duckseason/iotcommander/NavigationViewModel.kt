package mobi.duckseason.iotcommander

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NavigationViewModel : ViewModel() {

    private val _navigation = Channel<NavRoutes>()
    val navigation = _navigation.receiveAsFlow()
    
    fun onBackNavigation() {
        _navigation.trySend(NavRoutes.BACK)
    }
}