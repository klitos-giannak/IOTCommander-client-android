package mobi.duckseason.iotcommander.control

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import mobi.duckseason.iotcommander.discover.Device

@Immutable
data class ControlViewState(
    val device: Device?,
    val commands: List<CommandDescription>,
    val expandedCommands: List<CommandDescription> = emptyList()
) {
    companion object {
        val EMPTY = ControlViewState(null, emptyList())
    }
}

enum class ParameterType(private val value: String) {
    BOOLEAN("boolean"), INT("int"), FLOAT("float"), TEXT("text");

    fun validate(any: Any): Boolean {
        return when (this) {
            BOOLEAN -> any is Boolean
            INT -> any is Int
            FLOAT -> any is Float
            TEXT -> any is String
        }
    }

    companion object {
        fun fromInternalValue(searchTerm: String): ParameterType? {
            values().forEach {
                if (it.value == searchTerm) return it
            }
            return null
        }
    }
}

data class ParameterDescription(val name: String, val type: ParameterType)
data class CommandDescription(
    val name: String,
    val params: List<ParameterDescription> = emptyList()
)