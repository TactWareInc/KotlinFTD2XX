package net.tactware.ftdi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.tactware.ftdi.enums.BitModes
import net.tactware.ftdi.enums.FlowControl
import net.tactware.ftdi.enums.Parity
import net.tactware.ftdi.enums.StopBits
import net.tactware.ftdi.enums.WordLength

/**
 * Example showing how to use the KotlinFTD2XX library with coroutines and SharedFlows.
 */
class Example {
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Create a coroutine scope
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            
            try {
                println("Looking for FTDI devices...")
                
                // Open the first available device
                val device = FTDIDevice.openByIndex(0)
                println("Device opened: ${device.javaClass.name}")
                
                // Subscribe to data flow
                device.dataFlow.onEach { data ->
                    println("Received data (${data.size} bytes): ${String(data)}")
                }.launchIn(scope)
                
                // Subscribe to status updates
                device.statusFlow.onEach { status ->
                    println("Status: ${status.type} - ${status.message}")
                }.launchIn(scope)
                
                // Configure the device
                device.configure(
                    baudRate = 9600,
                    dataBits = WordLength.BITS_8,
                    stopBits = StopBits.ONE,
                    parity = Parity.NONE,
                    flowControl = FlowControl.NONE
                )
                
                // Start continuous reading
                device.startReading()
                
                // Example of writing data
                scope.launch {
                    val testData = "Test message from Kotlin\r\n".toByteArray()
                    println("Writing data: ${String(testData)}")
                    device.write(testData)
                }
                
                // Keep the application running
                println("Press Enter to exit")
                readLine()
                
                // Clean up
                device.close()
                println("Device closed")
                
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
