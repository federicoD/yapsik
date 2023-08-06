package com.federicod.yapsik

import java.net.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class PortScanner(private val ip: String,
				  private val port: Int,
                  private val semaphore: Semaphore,
                  private val plugin: ApplicationFingerprintPlugin?,
                  private val configuration: IConfiguration) {

	private val socket: Socket = Socket()
    private val readTimeout: Int = configuration.getReadTimeout()
    private val connectTimeout: Int = configuration.getConnectTimeout()
    private val waitTime: Long = 50

    private var isDone: Boolean = false
	private var thread: Thread? = null
	private var scanResult: PortScanResult = PortScanResult(port, false, null)

	fun waitUntilDone(maxWaitTime: Long) : PortScanResult {

        var waitedSoFar = 0L

		while (!isDone && waitedSoFar < maxWaitTime) {
			Thread.sleep(waitTime)
            waitedSoFar += waitTime
		}

        if (!isDone) {
            thread?.interrupt()
        }

		return scanResult
	}

	fun run() {

		// set read timeout
		socket.soTimeout = readTimeout

		semaphore.acquire()

		try {
			thread = thread(start = true) {

                var open = false
                var fingerprintResult: ApplicationFingerprintResult? = null

                try {
                    socket.connect(InetSocketAddress(InetAddress.getByName(ip), port), connectTimeout)
                    open = true

                    if (plugin != null) {
                        try {
                            fingerprintResult = plugin.run(socket)
                        }
                        catch (e: SocketTimeoutException) {
                            // read timeout
                        }
                        catch (e: Exception) {
                            println(e)
                        } finally {
                            socket.close()
                            isDone = true
                        }
                    }
                } catch (e: ConnectException) {
                } catch (e: SocketTimeoutException) {
                    // connect timeout
                } catch (e: Exception) {
                    println(e)
                } finally {
                    scanResult = PortScanResult(port, open, fingerprintResult)
                    isDone = true
                }
            }
		}
		catch (e: Exception) {
			println(e)
		}
		finally {
			semaphore.release()
		}
	}
}