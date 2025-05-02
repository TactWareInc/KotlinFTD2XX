package net.tactware.ftdi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.tactware.ftdi.core.FTDevice
import net.tactware.ftdi.core.FTDeviceManager
import net.tactware.ftdi.enums.BitModes
import net.tactware.ftdi.enums.FlowControl
import net.tactware.ftdi.enums.FT_STATUS
import net.tactware.ftdi.enums.Parity
import net.tactware.ftdi.enums.Purge
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
     * Return the device serial number
     */
    fun getSerialNumber() : String {
        return device.serialNumber
    }

    /**
     * Return the device description
     */
    fun getDescription() : String {
        return device.description
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
     * Set the GPIO when in bit bang mode
     * @param gpio Bit mode mask
     */
    fun setGPIO(gpio: Byte) {
        scope.launch {
            try {
                val mode = BitModes.fromValue(device.getBitMode())
                if(mode == BitModes.SYNC_BIT_BANG || mode == BitModes.ASYNC_BIT_BANG || mode == BitModes.CBUS_BIT_BANG) {
                    val data = ByteArray(5) { 0 }
                    data[0] = gpio
                    device.write(data)
                    _commandResponseFlow.emit(
                        CommandResponse(
                            CommandType.SET_GPIO,
                            success = true,
                            message = "Set GPIO command successful"
                        )
                    )
                } else {
                    throw FTD2XXException(FT_STATUS.FT_IO_ERROR, "Device is not in Bit Bang Mode)")
                }
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.SET_GPIO,
                    success = false,
                    message = "Set GPIO command failed: ${e.message}",
                    error = e
                ))
            }
        }
    }

    /**
     * Get the GPIO when in bit bang mode
     * @return gpio Bit mode mask
     */
    fun getGPIO(): Byte {
        val data = ByteArray(5) { 0 }
        val mode = BitModes.fromValue(device.getBitMode())
        if(mode == BitModes.SYNC_BIT_BANG || mode == BitModes.ASYNC_BIT_BANG || mode == BitModes.CBUS_BIT_BANG) {
            device.read(data,0,1)
        } else {
            throw FTD2XXException(FT_STATUS.FT_IO_ERROR, "Device is not in Bit Bang Mode)")
        }
        return data[0]
    }

    /**
     * Return device bit mode
     * @return Byte bit mode
     */
    fun getBitMode():Byte {
        return device.getBitMode()
    }
    
    /**
     * Close the device.
     */
    fun close() {
        stopReading()
        deviceManager.close()
    }

    /**
     * Purge the RX/TX buffers
     * @param purge flags for purging RX,TX or both
     */
    fun purge(purge: Purge) {
        scope.launch {
            try {
                device.purge(purge)
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.PURGE,
                    success = true,
                    message = "Purge successful"
                ))
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.PURGE,
                    success = false,
                    message = "Purge failed: ${e.message}",
                    error = e
                ))
            }
        }
    }

    /**
     * Reset the device
     */
    fun reset() {
        scope.launch {
            try {
                device.resetDevice()
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.RESET,
                    success = true,
                    message = "Reset successful"
                ))
            } catch (e: Exception) {
                _commandResponseFlow.emit(CommandResponse(
                    CommandType.RESET,
                    success = false,
                    message = "Rest failed: ${e.message}",
                    error = e
                ))
            }
        }
    }

    /**
     * This will make the chip flush its buffer back to the PC.
     * Only for MPSSE and MCU Host Emulation Modes
     * @return succes
     */
    fun flushbuffer(): Boolean {
        val mode = BitModes.fromValue(device.getBitMode())
        if( mode == BitModes.MPSSE || mode == BitModes.MCU_HOST_BUS_EMULATION) {
            val FTDI_MPSSE_COMMAND_FLUSH_BUFFER = 0x87.toByte()
            val data = ByteArray(5) { 0 }
            data[0] = FTDI_MPSSE_COMMAND_FLUSH_BUFFER
            device.write(data)
            return true
        }
        return false
    }

    /**
     * This function will perform a buffer flush/purge 3 different ways
     */
    fun hardPurge() {
        flushbuffer()
        device.purge(Purge.RX_TX)

        val data = ByteArray(4096)
        var status = device.getStatus()
        while (status.first > 0) {
            val bytesToRead = minOf(status.first, data.size)
            device.read(data, 0, bytesToRead)
            status = device.getStatus()
        }
    }

    /**
     * Return whether the device is open
     */
    fun isOpen(): Boolean {
        return device.isOpen()
    }
}

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
