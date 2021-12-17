package mobi.duckseason.iotcommander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mobi.duckseason.iotcommander.discover.DiscoverScreen
import mobi.duckseason.iotcommander.discover.DiscoverViewState
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            remember(viewModel.navigation, this) {
                viewModel.navigation.flowWithLifecycle(lifecycle)
            }.onEach { route ->
                when (route) {
                    NavRoutes.BACK -> {
                        if (!navController.popBackStack()) finish()
                    }
                    else -> navController.navigate(route.name)
                }
            }.launchIn(scope)

            val discoverViewState = remember(viewModel.discoverViewState, this) {
                viewModel.discoverViewState.flowWithLifecycle(lifecycle)
            }.collectAsState(DiscoverViewState.EMPTY)

            IOTCommanderTheme {
                NavHost(navController = navController, startDestination = NavRoutes.DISCOVER.name) {
                    composable(NavRoutes.DISCOVER.name) {
                        DiscoverScreen(
                            discoverViewState.value,
                            { viewModel.searchForDevices() },
                            { device -> viewModel.selectDevice(device) }
                        )
                    }
                }
            }
        }
    }
}