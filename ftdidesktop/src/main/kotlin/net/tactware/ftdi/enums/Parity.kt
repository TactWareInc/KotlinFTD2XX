package net.tactware.ftdi.enums

/**
 * Parity settings for FTDI devices.
 */
enum class Parity(val value: Int) {
    NONE(0),
    ODD(1),
    EVEN(2),
    MARK(3),
    SPACE(4);
    
    companion object {
        /**
         * Get Parity by value.
         * 
         * @param value The int value to look up
         * @return The corresponding Parity enum or null if not found
         */
        fun fromValue(value: Int): Parity? = entries.find { it.value == value }
    }
}
