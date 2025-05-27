package net.tactware.ftdi.exception

import net.tactware.ftdi.enums.FT_STATUS

/**
 * Exception thrown when FTD2XX operations fail.
 */
class FTD2XXException : Exception {
    /**
     * The FT_STATUS code associated with this exception.
     */
    val ftStatus: FT_STATUS

    /**
     * Constructs an FTD2XXException with the specified FT_STATUS.
     *
     * @param ftStatus The FT_STATUS code
     */
    constructor(ftStatus: FT_STATUS) : super("FTD2XX Error: ${ftStatus.name}") {
        this.ftStatus = ftStatus
    }

    /**
     * Constructs an FTD2XXException with the specified FT_STATUS and message.
     *
     * @param ftStatus The FT_STATUS code
     * @param message The detail message
     */
    constructor(ftStatus: FT_STATUS, message: String) : super("FTD2XX Error: ${ftStatus.name} - $message") {
        this.ftStatus = ftStatus
    }

    /**
     * Constructs an FTD2XXException with the specified FT_STATUS, message, and cause.
     *
     * @param ftStatus The FT_STATUS code
     * @param message The detail message
     * @param cause The cause of this exception
     */
    constructor(
        ftStatus: FT_STATUS,
        message: String,
        cause: Throwable
    ) : super("FTD2XX Error: ${ftStatus.name} - $message", cause) {
        this.ftStatus = ftStatus
    }

    /**
     * Constructs an FTD2XXException with the specified FT_STATUS and cause.
     *
     * @param ftStatus The FT_STATUS code
     * @param cause The cause of this exception
     */
    constructor(ftStatus: FT_STATUS, cause: Throwable) : super("FTD2XX Error: ${ftStatus.name}", cause) {
        this.ftStatus = ftStatus
    }
}
