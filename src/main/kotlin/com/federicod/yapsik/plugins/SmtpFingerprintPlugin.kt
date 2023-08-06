package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class SmtpFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Smtp"

    override fun isPortAccepted(port: Int): Boolean = port == 25

    override fun run(socket: Socket): ApplicationFingerprintResult {

        val validResponses = arrayOf(
            "220", "421", "500", "501", "504"
        )

        val data = ByteArray(3)

        val bytesRead = socket.getInputStream().read(data, 0, data.size)

        if (bytesRead == data.size) {

            val dataString = String(data)

            if (dataString in validResponses) {
                return ApplicationFingerprintResult(true, name)
            }
        }

        return ApplicationFingerprintResult(false, name)
    }
}

