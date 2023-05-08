package mobi.duckseason.iotcommander.discover

import androidx.compose.runtime.Immutable

@Immutable
data class DiscoverViewState(val devices: List<Device>, val loadingState: LoadingState) {
    companion object {
        val EMPTY = DiscoverViewState(emptyList(), LoadingState.NOT_LOADING)
        val NETWORK_ERROR = DiscoverViewState(emptyList(), LoadingState.ERROR)
    }

    enum class LoadingState {
        LOADING, NOT_LOADING, ERROR
    }
}