package net.tactware.ftdi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.tactware.ftdi.core.FTDevice
import net.tactware.ftdi.core.FTDeviceManager
import net.tactware.ftdi.enums.BitModes
import net.tactware.ftdi.enums.FlowControl
import net.tactware.ftdi.enums.Parity
import net.tactware.ftdi.enums.StopBits
import net.tactware.ftdi.enums.WordLength
import net.tactware.ftdi.exception.FTD2XXException

/**
 * Main entry point for the KotlinFTD2XX library.
 * Provides a simplified API for working with FTDI devices using Kotlin coroutines and flows.
 */
class FTDIDevice private constructor(
    private val device: FTDevice,
    private val deviceManager: FTDeviceManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Expose the SharedFlows from the device manager
    val dataFlow: SharedFlow<ByteArray> = deviceManager.dataFlow
    val statusFlow: SharedFlow<DeviceStatusUpdate> = deviceManager.statusFlow
    
    // Create a separate command response flow for request-response patterns
    private val _commandResponseFlow = MutableSharedFlow<CommandResponse>(
        replay = 0,
        extraBufferCapacity = 20
    )
    val commandResponseFlow: SharedFlow<CommandResponse> = _commandResponseFlow.asSharedFlow()
    
    companion object {
        /**
         * Open a device by index.
         *
         * @param index Index of the device to open
         * @return FTDIDevice instance
         * @throws FTD2XXException If device cannot be opened
         */
        @JvmStatic
        fun openByIndex(index: Int): FTDIDevice {
            val device = FTDevice.openByIndex(index)
            val manager = FTDeviceManager(device)
            return FTDIDevice(device, manager)
        }
        
        /**
         * Open a device by serial number.
         *
         * @param serialNumber Serial number of the device to open
         * @return FTDIDevice instance
         * @throws FTD2XXException If device cannot be opened
         */
        @JvmStatic
        fun openBySerialNumber(serialNumber: String): FTDIDevice {
            val device = FTDevice.openBySerialNumber(serialNumber)
            val manager = FTDeviceManager(device)
            return FTDIDevice(device, manager)
        }
    }
    
    /**
     * Start continuous reading from the device.
     * Data will be emitted to the dataFlow.
     *
     * @param bufferSize Size of the buffer for each read operation
     * @param pollIntervalMs Time in milliseconds between read operations
     */
    fun startReading(bufferSize: Int = 4096, pollIntervalMs: Long = 10) {
        deviceManager.startReading(bufferSize, pollIntervalMs)
    }
    
    /**
     * Stop continuous reading from the device.
     */
    fun stopReading() {
        deviceManager.stopReading()
    }
    
    /**
     * Configure the device with common settings.
     *
     * @param baudRate The baud rate to set
     * @param dataBits The data bits setting
     * @param stopBits The stop bits setting
     * @param parity The parity setting
     * @param flowControl The flow control setting
     */
    fun configure(
        baudRate: Int,
        dataBits: WordLength = WordLength.BITS_8,
        stopBits: StopBits = StopBits.ONE,
        parity: Parity = Parity.NONE,
        flowControl: FlowControl = FlowControl.NONE
    ) {
        scope.launch {
            try {
                device.setBaudRate(baudRate)
                device.setDataCharacteristics(dataBits, stopBits, parity)
                device.setFlowControl(flowControl)
                
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.CONFIGURE,
                    success = true,
                    message = "Device configured successfully"
                ))
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.CONFIGURE,
                    success = false,
                    message = "Configuration failed: ${e.message}",
                    error = e
                ))
            }
        }
    }
    
    /**
     * Write data to the device.
     *
     * @param data The data to write
     */
    fun write(data: ByteArray) {
        scope.launch {
            try {
                val bytesWritten = device.write(data)
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.WRITE,
                    success = true,
                    message = "Wrote $bytesWritten bytes",
                    data = bytesWritten
                ))
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.WRITE,
                    success = false,
                    message = "Write failed: ${e.message}",
                    error = e
                ))
            }
        }
    }
    
    /**
     * Set the bit mode for the device.
     *
     * @param mask Bit mode mask
     * @param mode Bit mode
     */
    fun setBitMode(mask: Byte, mode: BitModes) {
        scope.launch {
            try {
                device.setBitMode(mask, mode)
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.SET_BIT_MODE,
                    success = true,
                    message = "Bit mode set successfully"
                ))
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.SET_BIT_MODE,
                    success = false,
                    message = "Setting bit mode failed: ${e.message}",
                    error = e
                ))
            }
        }
    }
    
    /**
     * Close the device.
     */
    fun close() {
        stopReading()
        deviceManager.close()
    }
}

/**
 * Enum representing different types of commands.
 */
enum class CommandType {
    CONFIGURE,
    WRITE,
    SET_BIT_MODE
}

/**
 * Data class representing a command response.
 */
data class CommandResponse(
    val type: CommandType,
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val error: Throwable? = null
)

/**
 * Re-export DeviceStatusUpdate and DeviceStatusType from the core package
 * for easier access.
 */
typealias DeviceStatusUpdate = net.tactware.ftdi.core.DeviceStatusUpdate
typealias DeviceStatusType = net.tactware.ftdi.core.DeviceStatusType
