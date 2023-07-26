package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket

@Component
// Thanks to: https://github.com/praetorian-inc/fingerprintx/blob/main/pkg/plugins/services/redis/redis.go
class RedisFingerprintPlugin : ApplicationFingerprintPlugin {
    private val name = "Redis"

    override fun isPortAccepted(port: Int): Boolean  = port == 6379

    override fun run(socket: Socket): ApplicationFingerprintResult {
        // *1\r\n$4\r\nPING\r\n
        val ping = byteArrayOf(
            0x2a, 0x31, 0x0d, 0x0a, 0x24, 0x34, 0x0d, 0x0a, 0x50, 0x49, 0x4e, 0x47, 0x0d, 0x0a
        )

        // +PONG\r\n
        val pong = byteArrayOf(
            0x2b, 0x50, 0x4f, 0x4e, 0x47, 0x0d, 0x0a
        )

        socket.getOutputStream().write(ping)

        val nullByte : Byte = 0x0
        val inputStream = socket.getInputStream()
        var retries = 0

        while (retries < 10) {

            // I was using inputStream.readBytes() before but the process started hanging
            // https://discuss.kotlinlang.org/t/reading-socket-inputstream-hangs-the-whole-program/20988

            val data = ByteArray(100)
            val bytesRead = inputStream.read(data, 0, data.size)

            if (bytesRead == 0) {
                Thread.sleep(1000)
                retries++
                continue
            }

            if (bytesRead == 7) {

                if (data.copyOfRange(0, 7).contentEquals(pong)) {
                    return ApplicationFingerprintResult(true, name)
                }
            }

            return ApplicationFingerprintResult(false, name)
        }

        return ApplicationFingerprintResult(false, name)
    }
}