package net.tactware.ftdi.enums

enum class MPSSE_MCU_CMD(val CMD: Byte) {
    //Reference FTDI AN_233_Java_D2xx_for_Android_API_User_Manual.pdf
    //Reference FTDI AN_108_Command_Processor_for_MPSSE_and_MCU_Host_Bus_Emulation_Modes.pdf
    //Read Bytes Falling Edge LSB First

    READ_BYTES_RISING_EDGE(0x2B.toByte()),
    READ_BYTES_FALLING_EDGE(0x2C.toByte()),
    //Read/Write GPIO

    SET_DATA_BITS_LOW_BYTE(0x80.toByte()),
    READ_DATA_BITS_LOW_BYTE(0x81.toByte()),
    SET_DATA_BITS_HIGH_BYTE(0x82.toByte()),
    READ_DATA_BITS_HIGH_BYTE(0x83.toByte()),
    //Enable/Disable TDI/TDO Loopback

    ENABLE_LOOPBACK(0x84.toByte()),
    DISABLE_LOOPBACK(0x85.toByte()),
    CLOCK_DIVISOR(0X86.toByte()), //Set Clock Divisor


    SEND_IMMEDIATE(0X87.toByte()), //This will make the chip flush its buffer back to the PC.
    WAIT_ON_IO_HIGH(0x88.toByte()),

    WAIT_ON_IO_LOW(0x89.toByte()),
    CMD_60MHZ_CLOCK(0x8A.toByte()), //Disables the clk divide by 5 to allow for a 60MHz master clock.

    CMD_12MHZ_CLOCK(0x8B.toByte()), //Enables the clk divide by 5 to allow for a 12MHz master clock.
    ENABLE_3_PHASE_CLOCK(0x8C.toByte()),//Enables 3 phase data clocking. Used by I2C interfaces to allow data on both clock edges.
    DISABLE_3_PHASE_CLOCK(0x8D.toByte()), //Disables 3 phase data clocking.
    READ_SHORT_ADDRESS(0x90.toByte()),

    READ_EXTENDED_ADDRESS(0x91.toByte()),
    WRITE_SHORT_ADDRESS(0x92.toByte()),
    WRITE_EXTENDED_ADDRESS(0x93.toByte()),
    ENABLE_ADAPTIVE_CLOCK(0x96.toByte()),
    DISABLE_ADAPTIVE_CLOCK(0x97.toByte()),
    BOGUS(0xAA.toByte()), //Invalid command for testing

    BAD_CMD(0xFA.toByte()), //MPSEE response when a bad command is Sent
    FTDI_BYTE_MASK_INPUTS(0x00.toByte()),

    FTDI_BYTE_MASK_OUTPUTS(0xFF.toByte()),
}