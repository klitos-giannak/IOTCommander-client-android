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
import mobi.duckseason.iotcommander.control.ControlScreen
import mobi.duckseason.iotcommander.control.ControlViewModel
import mobi.duckseason.iotcommander.control.ControlViewState
import mobi.duckseason.iotcommander.discover.DiscoverScreen
import mobi.duckseason.iotcommander.discover.DiscoverViewModel
import mobi.duckseason.iotcommander.discover.DiscoverViewState
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

class MainActivity : ComponentActivity() {

    private lateinit var navigationVM: NavigationViewModel
    private lateinit var discoverVM: DiscoverViewModel
    private lateinit var controlVM: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigationVM = ViewModelProvider(this)[NavigationViewModel::class.java]
        discoverVM = ViewModelProvider(this)[DiscoverViewModel::class.java]
        controlVM = ViewModelProvider(this)[ControlViewModel::class.java]

        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            remember(navigationVM.navigation, this) {
                navigationVM.navigation.flowWithLifecycle(lifecycle)
            }.onEach { route ->
                when (route) {
                    NavRoutes.BACK -> {
                        if (!navController.popBackStack()) finish()
                    }
                    else -> navController.navigate(route.name)
                }
            }.launchIn(scope)

            val discoverViewState = remember(discoverVM.discoverViewState, this) {
                discoverVM.discoverViewState.flowWithLifecycle(lifecycle)
            }.collectAsState(DiscoverViewState.EMPTY)

            val controlViewState = remember(controlVM.controlViewState, this) {
                controlVM.controlViewState.flowWithLifecycle(lifecycle)
            }.collectAsState(ControlViewState.EMPTY)

            IOTCommanderTheme {
                NavHost(navController = navController, startDestination = NavRoutes.DISCOVER.name) {
                    composable(NavRoutes.DISCOVER.name) {
                        DiscoverScreen(
                            discoverViewState.value,
                            { discoverVM.searchForDevices() },
                            { device ->
                                controlVM.selectDevice(device)
                                navigationVM.navigateTo(NavRoutes.CONTROL)
                            }
                        )
                    }
                    composable(NavRoutes.CONTROL.name) {
                        ControlScreen(
                            controlViewState.value,
                            { commandDescription ->
                                controlVM.toggleExpanded(commandDescription)
                            },
                            { outGoingCommand ->
                                controlVM.onOutGoingCommand(outGoingCommand)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        navigationVM.onBackNavigation()
    }
}