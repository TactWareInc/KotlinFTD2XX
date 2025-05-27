package net.tactware.ftdi

/**
 * Data class representing a command response.
 */
data class CommandResponse(
    val type: CommandType,
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val error: Throwable? = null
)