package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class EchoFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Echo"

    override fun isPortAccepted(port: Int): Boolean = port == 7

    override fun run(socket: Socket): ApplicationFingerprintResult {

        // hello
        val request = byteArrayOf(
            0x68, 0x65, 0x6c, 0x6c, 0x6f
        )

        socket.getOutputStream().write(request)

        val readData = ByteArray(request.size)
        val bytesRead = socket.getInputStream().read(readData, 0, readData.size)
        val validResponse = bytesRead > 0 && String(readData) == String(request)

        return ApplicationFingerprintResult(validResponse, name)
    }
}