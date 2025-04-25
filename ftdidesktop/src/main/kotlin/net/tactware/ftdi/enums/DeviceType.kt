package net.tactware.ftdi.enums

/**
 * Device types for FTDI devices.
 */
enum class DeviceType(val value: Int) {
    FT_DEVICE_BM(0),
    FT_DEVICE_AM(1),
    FT_DEVICE_100AX(2),
    FT_DEVICE_UNKNOWN(3),
    FT_DEVICE_2232C(4),
    FT_DEVICE_232R(5),
    FT_DEVICE_2232H(6),
    FT_DEVICE_4232H(7),
    FT_DEVICE_232H(8),
    FT_DEVICE_X_SERIES(9),
    FT_DEVICE_4222H_0(10),
    FT_DEVICE_4222H_1_2(11),
    FT_DEVICE_4222H_3(12),
    FT_DEVICE_4222_PROG(13),
    FT_DEVICE_900(14),
    FT_DEVICE_930(15),
    FT_DEVICE_UMFTPD3A(16);
    
    companion object {
        /**
         * Get DeviceType by value.
         * 
         * @param value The int value to look up
         * @return The corresponding DeviceType enum or null if not found
         */
        fun fromValue(value: Int): DeviceType? = values().find { it.value == value }
    }
}
