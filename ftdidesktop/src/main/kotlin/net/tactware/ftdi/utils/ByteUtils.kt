package net.tactware.ftdi.utils

/**
 * Utility class for byte operations.
 */
object ByteUtils {
    /**
     * Convert a byte array to a hex string.
     *
     * @param bytes The byte array to convert
     * @return The hex string representation
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }

    /**
     * Convert a hex string to a byte array.
     *
     * @param hex The hex string to convert
     * @return The byte array
     */
    fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            result[i] = hex.substring(index, index + 2).toInt(16).toByte()
        }
        return result
    }

    /**
     * Convert an int to a byte array (little endian).
     *
     * @param value The int value to convert
     * @return The byte array (4 bytes)
     */
    fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    /**
     * Convert a byte array to an int (little endian).
     *
     * @param bytes The byte array to convert
     * @param offset The offset in the array
     * @return The int value
     */
    fun bytesToInt(bytes: ByteArray, offset: Int = 0): Int {
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }
}
