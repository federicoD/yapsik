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
        val reader = inputStream.bufferedReader()
        var retries = 0

        while (retries < 10) {
            if (!reader.ready()) {
                Thread.sleep(1000)
                retries++
                continue
            }

            val data = CharArray(4096)
            val bytesRead = reader.read(data, 0, data.size)
            val dataString = data.filter { it != '\u0000' }.joinToString("")

            var regex = Regex("^\\d{3}[- ](.*)\\r")

            if (regex.containsMatchIn(dataString)) {

                return ApplicationFingerprintResult(true, name, dataString)
            }
        }

        return ApplicationFingerprintResult(false, name)
    }
}