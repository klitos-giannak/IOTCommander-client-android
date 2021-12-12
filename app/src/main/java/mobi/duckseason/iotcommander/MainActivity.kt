package mobi.duckseason.iotcommander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mobi.duckseason.iotcommander.ui.theme.IOTCommanderTheme

import androidx.navigation.compose.rememberNavController
import mobi.duckseason.iotcommander.discover.DiscoverScreen
import mobi.duckseason.iotcommander.discover.DiscoverViewState

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            val scope = rememberCoroutineScope()
            val navigationRouteFlowLifecycleAware = remember(viewModel.navigation, this) {
                viewModel.navigation.flowWithLifecycle(lifecycle)
            }

            val navController = rememberNavController()

            navigationRouteFlowLifecycleAware
                .onEach { route ->
                    when (route) {
                        NavRoutes.BACK -> {
                            val isNavigated = navController.popBackStack()
                            if (!isNavigated)
                                finish()
                        }
                        else -> navController.navigate(route.name)
                    }
                }
                .launchIn(scope)

            val viewStateFlowLifecycleAware = remember(viewModel.discoverViewState, this) {
                viewModel.discoverViewState.flowWithLifecycle(lifecycle)
            }

            val discoverViewState by viewStateFlowLifecycleAware.collectAsState(
                DiscoverViewState.EMPTY
            )

            IOTCommanderTheme {
                NavHost(navController = navController, startDestination = NavRoutes.START.name) {
                    composable(NavRoutes.START.name) {
                        DiscoverScreen(discoverViewState)
                    }
                }
            }
        }
    }
}