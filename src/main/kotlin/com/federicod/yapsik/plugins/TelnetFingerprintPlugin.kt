package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class TelnetFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Telnet"

    override fun isPortAccepted(port: Int): Boolean  = port == 23

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

            if (bytesRead > 0) {
                val dataString = data.filter { it != '\u0000' }.joinToString("")

                if (dataString.indexOf("telnet", 0, true) != -1) {
                    return ApplicationFingerprintResult(true, name, dataString)
                }

                return ApplicationFingerprintResult(false, name, dataString)
            }
        }

        return ApplicationFingerprintResult(false, name)
    }
}

