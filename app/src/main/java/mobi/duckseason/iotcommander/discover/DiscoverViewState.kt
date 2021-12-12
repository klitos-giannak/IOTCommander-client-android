package mobi.duckseason.iotcommander.discover

import androidx.compose.runtime.Immutable

@Immutable
data class DiscoverViewState(val devices:List<Device>) {
    companion object{
        val EMPTY = DiscoverViewState(emptyList())
    }
}