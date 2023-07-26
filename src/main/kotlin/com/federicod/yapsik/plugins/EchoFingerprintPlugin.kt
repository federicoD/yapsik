package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
class EchoFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Echo"

    override fun isPortAccepted(port: Int): Boolean  = port == 7

    override fun run(socket: Socket): ApplicationFingerprintResult {

        // hello
        val request = byteArrayOf(
            0x68, 0x65, 0x6c, 0x6c, 0x6f
        )

        socket.getOutputStream().write(request)

        val inputStream = socket.getInputStream()
        val reader = inputStream.bufferedReader()
        var retries = 0

        while (retries < 10) {
            if (!reader.ready()) {
                Thread.sleep(1000)
                retries++
                continue
            }

            val data = CharArray(request.size)
            val bytesRead = reader.read(data, 0, data.size)

            if (bytesRead > 0) {
                val dataString = data.filter { it != '\u0000' }.joinToString("")

                if (dataString == String(request)) {
                    return ApplicationFingerprintResult(true, name)
                }
            }
        }

        return ApplicationFingerprintResult(false, name)
    }
}