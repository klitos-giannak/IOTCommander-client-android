package mobi.duckseason.iotcommander.control

import androidx.compose.runtime.Immutable
import mobi.duckseason.iotcommander.discover.Device

@Immutable
class ControlViewState(val device: Device?, val output:String) {
    companion object {
        val EMPTY = ControlViewState(null, "")
    }
}