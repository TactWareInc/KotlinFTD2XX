package net.tactware.ftdi.enums

/**
 * Enum representing different types of commands.
 */
enum class CommandType {
    CONFIGURE,
    WRITE,
    SET_BIT_MODE,
    SET_GPIO,
    GET_GPIO,
    PURGE,
    RESET
}