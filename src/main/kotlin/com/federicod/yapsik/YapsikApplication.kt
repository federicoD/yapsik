package com.federicod.yapsik

import com.federicod.yapsik.plugins.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

@SpringBootApplication
class YapsikApplication

private val timeout = 1000

// From shodan.io
private val ipAddresses = listOf(
	// Echo
	"213.42.99.89",
	"5.226.109.177",
	"202.27.56.228",
	// Redis
	"54.208.146.203",
	"212.109.219.137",
	"18.221.7.126",
	"114.132.126.222",
	"18.182.33.90",
	"211.148.131.17",
	"18.132.68.155",
	"3.110.205.60",
	// Telnet
	"39.98.16.43",
	"203.210.239.201",
	"116.237.249.214",
	"120.24.143.248",
	"87.249.99.146",
	// Kafka
	"47.108.138.70",
	"13.245.64.59",
	"139.224.2.48",
	"103.160.90.215",
	// FTP
	"203.130.179.234",
	"211.248.145.135",
	"14.34.8.38",
	"175.198.145.185",
	"211.199.106.177",
	"125.129.172.137"
)

private val commonPorts = listOf(
	7, // echo
	21, // FTP
	23, // Telnet
	25, // SMTP
	9092, // Kafka no TLS
	6379 // Redis
)

private val threadsLimitSemaphore : Semaphore = Semaphore(5)

fun main(args: Array<String>) {
	val appContext = runApplication<YapsikApplication>(*args)

	val scanner = appContext.getBean<Scanner>()

	for (ip in ipAddresses) {

		try {

			println("Scanning ip $ip...")

			val scannedPorts = scanner.scanIp(ip)

			println()

			scannedPorts.forEach {

				if (it.value != null) {
					println("Port ${it.key} opened")
					println("Application: ${it.value?.applicationType}")
					println("Valid response: ${it.value?.gotValidResponse}")
					println()
				}
			}

			println("======================================================")
		}
		catch (e: Exception) {

			println("Error scanning ip $ip")
			println(e)
		}
	}
}

@Component
class Scanner(
	@Autowired private val plugins: List<ApplicationFingerprintPlugin>
) {
	fun scanIp(ip: String) : MutableMap<Int, ApplicationFingerprintResult?> {

		// I should probably replace it with a thread-safe collection
		val scannedPorts = mutableMapOf<Int, ApplicationFingerprintResult?>()

		val threads = commonPorts.map {

			threadsLimitSemaphore.acquire()

			thread {

				val port = it
				val socket = Socket()
				var result: ApplicationFingerprintResult? = null

				try {
					socket.connect(InetSocketAddress(InetAddress.getByName(ip), port), timeout)

					try {

						for (plugin in plugins) {
							if (plugin.isPortAccepted(port)) {
								result = plugin.run(socket)
							}
						}

						socket.close()
					} catch (e: Exception) {
						println(e)
					}
				} catch (e: ConnectException) {
				} catch (e: SocketTimeoutException) {
				} catch (e: Exception) {
					println(e)
				} finally {
					scannedPorts[port] = result

					threadsLimitSemaphore.release()
				}
			}
		}

		for (t in threads) t.join()

		return scannedPorts
	}
}



