package mobi.duckseason.iotcommander.discover

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverResponse(val deviceName: String)

data class Device(val name:String, val ip:String)