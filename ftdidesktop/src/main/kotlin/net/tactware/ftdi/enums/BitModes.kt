package net.tactware.ftdi.enums

/**
 * Bit modes for FTDI devices.
 */
enum class BitModes(val value: Byte) {
    RESET(0x0),
    ASYNC_BIT_BANG(0x1),
    MPSSE(0x2),
    SYNC_BIT_BANG(0x4),
    MCU_HOST_BUS_EMULATION(0x8),
    FAST_OPTO_ISOLATED_SERIAL(0x10),
    CBUS_BIT_BANG(0x20),
    SINGLE_CHANNEL_SYNC_245_FIFO(0x40);

    companion object {
        /**
         * Get BitMode by value.
         *
         * @param value The byte value to look up
         * @return The corresponding BitModes enum or null if not found
         */
        fun fromValue(value: Byte): BitModes? = entries.find { it.value == value }
    }
}
