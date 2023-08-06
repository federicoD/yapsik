package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class FtpFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "FTP"

    override fun isPortAccepted(port: Int) = port == 21

    override fun run(socket: Socket): ApplicationFingerprintResult {

        val inputStream = socket.getInputStream()

        val data = ByteArray(1024)
        val bytesRead = inputStream.read(data, 0, data.size)

        var regex = Regex("^\\d{3}[- ](.*)\\r")

        if (regex.containsMatchIn(String(data))) {

            return ApplicationFingerprintResult(true, name)
        }

        return ApplicationFingerprintResult(false, name)
    }
}