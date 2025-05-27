package net.tactware.ftdi.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import net.tactware.ftdi.enums.FlowControl
import net.tactware.ftdi.enums.Parity
import net.tactware.ftdi.enums.StopBits
import net.tactware.ftdi.enums.WordLength
import net.tactware.ftdi.exception.FTD2XXException

/**
 * Extension functions for FTDevice to provide coroutine-based operations.
 */
object FTDeviceExtensions {

    /**
     * Read data from the device as a Flow.
     * This function creates a Flow that emits ByteArrays as they are read from the device.
     *
     * @param device The FTDevice to read from
     * @param bufferSize Size of the buffer for each read operation
     * @param pollIntervalMs Time in milliseconds between read operations when no data is available
     * @return Flow of ByteArrays containing data read from the device
     */
    fun readAsFlow(
        device: FTDevice,
        bufferSize: Int = 4096,
        pollIntervalMs: Long = 10
    ): Flow<ByteArray> = flow {
        val buffer = ByteArray(bufferSize)

        while (true) {
            val (rxBytes, _, _) = device.getStatus()

            if (rxBytes > 0) {
                val bytesToRead = minOf(rxBytes, bufferSize)
                val bytesRead = device.read(buffer, 0, bytesToRead)

                if (bytesRead > 0) {
                    emit(buffer.copyOf(bytesRead))
                }
            } else {
                kotlinx.coroutines.delay(pollIntervalMs)
            }
        }
    }

    /**
     * Suspend function to write data to the device.
     *
     * @param device The FTDevice to write to
     * @param data The data to write
     * @return Number of bytes written
     */
    suspend fun writeAsync(device: FTDevice, data: ByteArray): Int = withContext(Dispatchers.IO) {
        device.write(data)
    }

    /**
     * Suspend function to read a specific number of bytes from the device.
     * This function will wait until the requested number of bytes is available.
     *
     * @param device The FTDevice to read from
     * @param length Number of bytes to read
     * @param timeout Timeout in milliseconds, or 0 for no timeout
     * @return ByteArray containing the read data
     * @throws FTD2XXException If a timeout occurs or another error happens
     */
    suspend fun readExactAsync(
        device: FTDevice,
        length: Int,
        timeout: Long = 0
    ): ByteArray = withContext(Dispatchers.IO) {
        val buffer = ByteArray(length)
        var totalRead = 0
        val startTime = System.currentTimeMillis()

        while (totalRead < length) {
            if (timeout > 0 && System.currentTimeMillis() - startTime > timeout) {
                throw FTD2XXException(
                    net.tactware.ftdi.enums.FT_STATUS.FT_OTHER_ERROR,
                    "Timeout while reading data"
                )
            }

            val (rxBytes, _, _) = device.getStatus()

            if (rxBytes > 0) {
                val bytesToRead = minOf(rxBytes, length - totalRead)
                val bytesRead = device.read(buffer, totalRead, bytesToRead)
                totalRead += bytesRead
            } else {
                kotlinx.coroutines.delay(10)
            }
        }

        buffer
    }

    /**
     * Suspend function to configure the device with common settings.
     *
     * @param device The FTDevice to configure
     * @param baudRate The baud rate to set
     * @param dataBits The data bits setting
     * @param stopBits The stop bits setting
     * @param parity The parity setting
     * @param flowControl The flow control setting
     */
    suspend fun configureAsync(
        device: FTDevice,
        baudRate: Int,
        dataBits: WordLength = WordLength.BITS_8,
        stopBits: StopBits = StopBits.ONE,
        parity: Parity = Parity.NONE,
        flowControl: FlowControl = FlowControl.NONE
    ) = withContext(Dispatchers.IO) {
        device.setBaudRate(baudRate)
        device.setDataCharacteristics(dataBits, stopBits, parity)
        device.setFlowControl(flowControl)
    }
}
