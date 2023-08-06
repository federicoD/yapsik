package com.federicod.yapsik

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore

@Component
class IpScanner(
	@Autowired private val configuration: IConfiguration,
	@Autowired private val plugins: List<ApplicationFingerprintPlugin>
) {
	private val threadsLimitSemaphore : Semaphore = Semaphore(configuration.getConcurrencyLimit())
	private val commonPorts : List<Int> = configuration.getCommonPorts()
	private val maxWaitTime : Long = configuration.getMaxWaitTimeForPortScan()

	fun scanIp(ip: String) : List<PortScanResult> {

		// During initialization, we enforce to have one plugin for one port.
		// For this reason we can avoid using a thread-safe collection
		val scannedPorts = mutableListOf<PortScanResult>()

		val portScanners = commonPorts.map {

			val plugin = plugins.firstOrNull { p -> p.isPortAccepted(it) }

			// TODO: Make it testable

			val runner = PortScanner(ip, it, threadsLimitSemaphore, plugin, configuration)

			runner.run()

			runner
		}

		for (runner in portScanners) {

			try {

				val res = runner.waitUntilDone(maxWaitTime)

				scannedPorts.add(res)
			}
			catch (e: InterruptedException) {
				println(e)
			}
			catch (e: Exception) {
				println(e)
			}
		}

		return scannedPorts.toList()
	}
}