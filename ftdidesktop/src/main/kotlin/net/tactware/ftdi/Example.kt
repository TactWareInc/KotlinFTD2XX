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
import net.tactware.ftdi.enums.Purge
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
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            
            try {
                println("Looking for FTDI devices...")
                
                // Open the first available device
                var device = FTDIDevice.openByIndex(0)
                println("Device opened: ${device.javaClass.name} ${device.getSerialNumber()} ${device.getDescription()}")

                //Test out functions
                if(device.isOpen())
                    println("Device is open.")

                //Test Bit Bang GPIO
                val outPuts = ByteArray(1) { 0x7B.toByte() }
                device.setBitMode(0xFF.toByte(),BitModes.ASYNC_BIT_BANG) //0xFF for out pins as outputs
                device.write(outPuts) //Bits are set at default interal baud rate
                Thread.sleep(100)
                var bitReg = device.getBitMode()
                if(bitReg == outPuts[0])
                    println("Bit Bang test complete")
                else
                    println("Bit Bang test fail. Maybe do to your physical hardware connection")

                //Rest Device
                device.reset()
                device.purge(Purge.RX_TX)
                device.setBitMode(0x00,BitModes.RESET)

                //Test MPSSE Mode
                device.setBitMode(0x00, mode = BitModes.MPSSE)

                if(device.testMPSSE()) {
                    println("MPSSE test passed.")
                } else {
                    println("MPSSE test fail, device may not support it.")
                }


                device.purge(Purge.RX_TX)

                device.hardPurge()

                device.reset()

                device.close()
                if(!device.isOpen())
                    println("Device is closed.")

                // Repen the first available device
                device = FTDIDevice.openByIndex(0)
                println("Device opened: ${device.javaClass.name} ${device.getSerialNumber()} ${device.getDescription()}")
                
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
