package mobi.duckseason.iotcommander.discover

import kotlinx.serialization.Serializable

@Serializable
data class Device(val name:String, val ip:String)