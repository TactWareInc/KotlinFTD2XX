package net.tactware.ftdi.enums

/**
 * Word length settings for FTDI devices.
 */
enum class WordLength(val value: Int) {
    BITS_8(0),
    BITS_7(1),
    BITS_6(2),
    BITS_5(3);
    
    companion object {
        /**
         * Get WordLength by value.
         * 
         * @param value The int value to look up
         * @return The corresponding WordLength enum or null if not found
         */
        fun fromValue(value: Int): WordLength? = entries.find { it.value == value }
    }
}
