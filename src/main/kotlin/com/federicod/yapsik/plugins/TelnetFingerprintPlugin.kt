package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class TelnetFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Telnet"

    override fun isPortAccepted(port: Int): Boolean = port == 23

    override fun run(socket: Socket): ApplicationFingerprintResult {

        val inputStream = socket.getInputStream()

        val data = ByteArray(1024)
        val bytesRead = inputStream.read(data, 0, data.size)

        if (bytesRead > 0) {
            val dataString = String(data)

            if (dataString.indexOf("telnet", 0, true) != -1) {
                return ApplicationFingerprintResult(true, name)
            }

            return ApplicationFingerprintResult(false, name)
        }

        return ApplicationFingerprintResult(false, name)
    }
}

