package net.tactware.ftdi.enums

/**
 * Status codes returned by FTD2XX functions.
 */
enum class FT_STATUS(val value: Int) {
    FT_OK(0),
    FT_INVALID_HANDLE(1),
    FT_DEVICE_NOT_FOUND(2),
    FT_DEVICE_NOT_OPENED(3),
    FT_IO_ERROR(4),
    FT_INSUFFICIENT_RESOURCES(5),
    FT_INVALID_PARAMETER(6),
    FT_INVALID_BAUD_RATE(7),
    FT_DEVICE_NOT_OPENED_FOR_ERASE(8),
    FT_DEVICE_NOT_OPENED_FOR_WRITE(9),
    FT_FAILED_TO_WRITE_DEVICE(10),
    FT_EEPROM_READ_FAILED(11),
    FT_EEPROM_WRITE_FAILED(12),
    FT_EEPROM_ERASE_FAILED(13),
    FT_EEPROM_NOT_PRESENT(14),
    FT_EEPROM_NOT_PROGRAMMED(15),
    FT_INVALID_ARGS(16),
    FT_NOT_SUPPORTED(17),
    FT_OTHER_ERROR(18),
    FT_DEVICE_LIST_NOT_READY(19);
    
    companion object {
        /**
         * Get FT_STATUS by value.
         * 
         * @param value The int value to look up
         * @return The corresponding FT_STATUS enum or null if not found
         */
        fun fromValue(value: Int): FT_STATUS? = entries.find { it.value == value }
    }
}
