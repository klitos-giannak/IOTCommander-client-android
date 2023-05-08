package mobi.duckseason.iotcommander.discover

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import kotlin.math.pow


private const val LOCATOR_MESSAGE = "{\"action\":\"discover\"}"
private const val DISCOVERY_PORT = 9977
private const val SEARCH_DURATION_MILLIS = 5000L
private const val SEARCH_INTERVAL_MILLIS = 500L
private const val RECEIVE_BUFFER_SIZE = 1500

private val TAG = DeviceDiscoverer::class.java.simpleName


class DeviceDiscoverer(
    private val appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val devices = mutableSetOf<Device>()

    private val _devicesFlow: MutableStateFlow<Set<Device>> = MutableStateFlow(devices.toSet())
    val devicesFlow: StateFlow<Set<Device>> = _devicesFlow.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching = _searching.asStateFlow()
    private val _networkError = MutableStateFlow(false)
    val networkError = _networkError.asStateFlow()

    private fun resetFound() {
        devices.clear()
        _devicesFlow.value = devices.toSet()
    }

    suspend fun invokeSearch() = withContext(ioDispatcher) {
        if (_searching.compareAndSet(expect = false, update = true)) {
            resetFound()
            _networkError.value = false

            @Suppress("BlockingMethodInNonBlockingContext") // false positive. Linter cannot recognise ioDispatcher is Dispatchers.IO
            val datagramSocket = try {
                DatagramSocket().apply {
                    broadcast = true
                    soTimeout = 3000
                }
            } catch (ex: SocketException) {
                Log.e(TAG, "Unable to create UDP Socket", ex)
                null
            }

            datagramSocket?.use { socket ->
                val packet = createOutgoingPacket()

                val searchUntil = System.currentTimeMillis() + SEARCH_DURATION_MILLIS

                if (packet.address == null) {
                    _networkError.value = true
                } else {
                    while (System.currentTimeMillis() < searchUntil) {
                        socket.send(packet)

                        val buffer = ByteArray(RECEIVE_BUFFER_SIZE)
                        val receiver = DatagramPacket(buffer, buffer.size)

                        val decodeFromString = try {
                            socket.receive(receiver)
                            val received = String(buffer, 0, receiver.length)
                            Log.d(TAG, "Received: $received")
                            Json.decodeFromString(DiscoverResponse.serializer(), received)
                        } catch (ex: IOException) {
                            Log.e(TAG, "Error while Receiving packet", ex)
                            null
                        } catch (ex: SerializationException) {
                            Log.d(TAG, "Unable to parse received packet into a Device")
                            null
                        }

                        decodeFromString?.let { discoverResponse ->
                            receiver.address.hostAddress?.let { ipAddress ->
                                devices.add(Device(name = discoverResponse.deviceName, ip = ipAddress))
                                _devicesFlow.tryEmit(devices.toSet())
                            }
                        }

                        delay(SEARCH_INTERVAL_MILLIS)
                    }
                }
                _searching.value = false
            }
        }
    }

    private fun createOutgoingPacket(): DatagramPacket {
        val message: ByteArray = LOCATOR_MESSAGE.toByteArray()
        return DatagramPacket(
            message,
            LOCATOR_MESSAGE.length,
            getBroadcastAddress(),
            DISCOVERY_PORT
        )
    }

    private fun getBroadcastAddress(): InetAddress? {
        val addresses =
            (appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                .let { connectivityManager ->
                    connectivityManager?.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses
                }

        //From docs: Typically a link will have one IPv4 address and one or more IPv6 addresses.
        addresses?.forEach { linkAddress ->
            (linkAddress.address as? Inet4Address)?.let { address ->
                // Calculate the netmask as int
                val maskBits = 32 - linkAddress.prefixLength
                val maskAsInt = 2.0.pow(8).toInt() - 1

                val ipAsInt = ByteBuffer.wrap(address.address).int

                // Shift right and then left to clean the maskBits. Then add the mask
                val broadcastAsInt = ((ipAsInt shr maskBits) shl maskBits) + maskAsInt
                val broadcastAsByteBuffer = ByteBuffer.allocate(4).putInt(broadcastAsInt).array()
                return try {
                    InetAddress.getByAddress(broadcastAsByteBuffer)
                } catch (ex: UnknownHostException) {
                    Log.e(TAG, "Unable to create InetAddress", ex)
                    null
                }
            }
        }

        //if we haven't found the broadcast address until now
        return null

        //Alternative using WifiManager but getDhcpInfo() is deprecated in API level 31
//        val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val dhcp = wifi.dhcpInfo
//        // handle null somehow
//        if (dhcp == null) {
//            Log.d("onionsearch", "no dhcp info found")
//            return null
//        }
//        val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
//        val quads = ByteArray(4)
//        for (k in 0..3) quads[k] = (broadcast shr k * 8).toByte()
//        return try {
//            InetAddress.getByAddress(quads)
//        } catch (e: UnknownHostException) {
//            e.printStackTrace()
//            null
//        }
    }
}