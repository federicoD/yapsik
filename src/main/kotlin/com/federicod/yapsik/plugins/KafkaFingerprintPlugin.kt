package com.federicod.yapsik.plugins

import com.federicod.yapsik.ApplicationFingerprintPlugin
import com.federicod.yapsik.ApplicationFingerprintResult
import org.springframework.stereotype.Component
import java.net.Socket
import java.nio.ByteBuffer

@Component
// Thanks to: https://github.com/praetorian-inc/fingerprintx/blob/main/pkg/plugins/services/kafka/kafkaNew/kafkaNew.go
class KafkaFingerprintPlugin : ApplicationFingerprintPlugin {

    private val name = "Kafka"

    override fun isPortAccepted(port: Int): Boolean = port == 9092

    override fun run(socket: Socket): ApplicationFingerprintResult {

        val cid = arrayOf<Byte>(
            0x1e, 0x33, 0x12, 0x23
        )

        val apiVersionsRequest = arrayOf<Byte>(
            /* length */
            0x00, 0x00, 0x00, 0x43,
            /* request_api_key */
            0x00, 0x12,
            /* request_api_version */
            0x00, 0x00,
            /* correlation_id */
            cid[0], cid[1], cid[2], cid[3],
            /* client_id */
            0x00, 0x1f, 0x63, 0x6f, 0x6e, 0x73, 0x75, 0x6d,
            0x65, 0x72, 0x2d, 0x4f, 0x66, 0x66, 0x73, 0x65,
            0x74, 0x20, 0x45, 0x78, 0x70, 0x6c, 0x6f, 0x72,
            0x65, 0x72, 0x20, 0x32, 0x2e, 0x32, 0x2d, 0x31,
            0x38,
            /* TAG_BUFFER */
            0x00,
            /* client_software_name */
            0x12, 0x61, 0x70, 0x61, 0x63, 0x68, 0x65, 0x2d,
            0x6b, 0x61, 0x66, 0x6b, 0x61, 0x2d, 0x6a, 0x61,
            0x76, 0x61,
            /* client_software_version */
            0x06, 0x32, 0x2e, 0x34, 0x2e, 0x30,
            /* _tagged_fields */
            0x00,
        )

        socket.getOutputStream().use { stream ->
            stream.write(apiVersionsRequest.toByteArray())
        }

        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()

        outputStream.write(apiVersionsRequest.toByteArray())

        val data = ByteArray(8)
        val bytesRead = inputStream.read(data, 0, data.size)

        val responseLength = ByteBuffer.wrap(data.copyOfRange(0, 4)).int
        val responseCorrelationId = data.copyOfRange(4, 8)

        // remove the first 4 bytes (used to get the length)
        if (responseLength != bytesRead - 4) {
            return ApplicationFingerprintResult(false, name)
        }

        for (i in responseCorrelationId.indices) {
            if (responseCorrelationId[i] != cid[i]) {
                return ApplicationFingerprintResult(false, name)
            }
        }

        return ApplicationFingerprintResult(true, name)
    }
}