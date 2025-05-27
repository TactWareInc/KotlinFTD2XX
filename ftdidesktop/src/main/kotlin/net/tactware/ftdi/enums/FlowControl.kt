package net.tactware.ftdi.enums

/**
 * Flow control flags for FTDI devices.
 */
enum class FlowControl(val value: Int) {
    NONE(0),
    RTS_CTS(0x100),
    DTR_DSR(0x200),
    XON_XOFF(0x400);

    companion object {
        /**
         * Get FlowControl by value.
         *
         * @param value The int value to look up
         * @return The corresponding FlowControl enum or null if not found
         */
        fun fromValue(value: Int): FlowControl? = entries.find { it.value == value }
    }
}
