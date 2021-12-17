package mobi.duckseason.iotcommander.discover

import androidx.compose.runtime.Immutable

@Immutable
data class DiscoverViewState(val devices: List<Device>, val loadingState: Boolean) {
    companion object {
        val EMPTY = DiscoverViewState(emptyList(), false)
    }
}