package net.tactware.ftdi.enums

/**
 * Purge flags for FTDI devices.
 */
enum class Purge(val value: Int) {
    RX(1),
    TX(2),
    RX_TX(3);
    
    companion object {
        /**
         * Get Purge by value.
         * 
         * @param value The int value to look up
         * @return The corresponding Purge enum or null if not found
         */
        fun fromValue(value: Int): Purge? = entries.find { it.value == value }
    }
}
