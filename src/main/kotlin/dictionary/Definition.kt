package dictionary

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Definition(
    @SerialName("definition") val definition: String
)