package net.tactware.ftdi.enums

/**
 * Device status flags.
 */
enum class DeviceStatus(val value: Int) {
    OPENED(0),
    OPEN_NOTSET(1),
    OPEN_FAILED(2);
    
    companion object {
        /**
         * Get DeviceStatus by value.
         * 
         * @param value The int value to look up
         * @return The corresponding DeviceStatus enum or null if not found
         */
        fun fromValue(value: Int): DeviceStatus? = entries.find { it.value == value }
    }
}
