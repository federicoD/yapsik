package com.federicod.yapsik

import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.concurrent.Semaphore

@SpringBootApplication
class YapsikApplication

// From shodan.io
private val ipAddresses = listOf(
	// Smtp
	"218.216.70.17",
	"210.198.120.171",
	"210.129.53.158",
	"158.201.249.2",
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

fun main(args: Array<String>) {
	val appContext = runApplication<YapsikApplication>(*args)

	// TODO: Assert one plugin per port

	val scanner = appContext.getBean<IpScanner>()

	for (ip in ipAddresses) {

		try {

			println("Scanning ip $ip...")

			val scannedPorts = scanner.scanIp(ip)

			println()

			scannedPorts.forEach {

				if (it.open) {
					println("Port ${it.port} is open")

					if (it.fingerprintResult != null) {
						println("Valid response: ${it.fingerprintResult?.gotValidResponse}")
						println("Application: ${it.fingerprintResult?.applicationType}")
						println()
					}
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


