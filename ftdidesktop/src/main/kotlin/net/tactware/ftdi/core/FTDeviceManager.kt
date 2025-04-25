package net.tactware.ftdi.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.tactware.ftdi.exception.FTD2XXException
import java.io.Closeable

/**
 * A device manager that handles communication with FTDevice using coroutines.
 * Provides reactive data streams using SharedFlow.
 */
class FTDeviceManager(private val device: FTDevice) : Closeable {
    
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var readJob: Job? = null
    private var isReading = false
    
    // SharedFlow for received data with replay and buffer capacity
    private val _dataFlow = MutableSharedFlow<ByteArray>(
        replay = 0,
        extraBufferCapacity = 100
    )
    
    /**
     * SharedFlow that emits data received from the device.
     */
    val dataFlow: SharedFlow<ByteArray> = _dataFlow.asSharedFlow()
    
    // SharedFlow for device status updates
    private val _statusFlow = MutableSharedFlow<DeviceStatusUpdate>(
        replay = 1,
        extraBufferCapacity = 10
    )
    
    /**
     * SharedFlow that emits device status updates.
     */
    val statusFlow: SharedFlow<DeviceStatusUpdate> = _statusFlow.asSharedFlow()
    
    /**
     * Start continuous reading from the device in a coroutine.
     * Data will be emitted to the dataFlow.
     *
     * @param bufferSize Size of the buffer for each read operation
     * @param pollIntervalMs Time in milliseconds between read operations
     */
    fun startReading(bufferSize: Int = 4096, pollIntervalMs: Long = 10) {
        if (isReading) return
        
        isReading = true
        _statusFlow.tryEmit(DeviceStatusUpdate(DeviceStatusType.READING_STARTED))
        
        readJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            
            try {
                while (isReading) {
                    val (rxBytes, _, _) = device.getStatus()
                    
                    if (rxBytes > 0) {
                        val bytesToRead = minOf(rxBytes, bufferSize)
                        val bytesRead = device.read(buffer, 0, bytesToRead)
                        
                        if (bytesRead > 0) {
                            val data = buffer.copyOf(bytesRead)
                            _dataFlow.emit(data)
                        }
                    }
                    
                    kotlinx.coroutines.delay(pollIntervalMs)
                }
            } catch (e: Exception) {
                _statusFlow.tryEmit(DeviceStatusUpdate(
                    DeviceStatusType.ERROR,
                    "Error during read: ${e.message}"
                ))
                stopReading()
            }
        }
    }
    
    /**
     * Stop continuous reading from the device.
     */
    fun stopReading() {
        if (!isReading) return
        
        isReading = false
        readJob?.cancel()
        readJob = null
        _statusFlow.tryEmit(DeviceStatusUpdate(DeviceStatusType.READING_STOPPED))
    }
    
    /**
     * Write data to the device asynchronously.
     *
     * @param data The data to write
     * @return Job that can be used to track the write operation
     */
    fun writeAsync(data: ByteArray): Job {
        return scope.launch {
            try {
                val bytesWritten = device.write(data)
                _statusFlow.emit(DeviceStatusUpdate(
                    DeviceStatusType.WRITE_COMPLETED,
                    "Wrote $bytesWritten bytes"
                ))
            } catch (e: FTD2XXException) {
                _statusFlow.emit(DeviceStatusUpdate(
                    DeviceStatusType.ERROR,
                    "Write error: ${e.message}"
                ))
                throw e
            }
        }
    }
    
    /**
     * Configure the device with common settings.
     *
     * @param baudRate The baud rate to set
     */
    suspend fun configure(baudRate: Int) {
        try {
            device.setBaudRate(baudRate)
            _statusFlow.emit(DeviceStatusUpdate(
                DeviceStatusType.CONFIGURED,
                "Device configured with baud rate $baudRate"
            ))
        } catch (e: FTD2XXException) {
            _statusFlow.emit(DeviceStatusUpdate(
                DeviceStatusType.ERROR,
                "Configuration error: ${e.message}"
            ))
            throw e
        }
    }
    
    /**
     * Close the device manager and the underlying device.
     */
    override fun close() {
        stopReading()
        device.close()
        _statusFlow.tryEmit(DeviceStatusUpdate(DeviceStatusType.CLOSED))
    }
}

/**
 * Enum representing different types of device status updates.
 */
enum class DeviceStatusType {
    CONFIGURED,
    READING_STARTED,
    READING_STOPPED,
    WRITE_COMPLETED,
    ERROR,
    CLOSED
}

/**
 * Data class representing a device status update.
 */
data class DeviceStatusUpdate(
    val type: DeviceStatusType,
    val message: String = ""
)
