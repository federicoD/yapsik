package com.federicod.yapsik

import org.springframework.stereotype.Component

@Component
class Configuration : IConfiguration {

	private val connectTimeout = 2000
	// The SMTP plugin seems to work only with a quite high readTimeout (30s or more)
	private val readTimeout = 40000
	private val maxWaitTime = connectTimeout + readTimeout + 1000L
	private val concurrencyLimit = 10

	private val commonPorts = listOf(
		7, // echo
		21, // FTP
		23, // Telnet
		25, // SMTP
		9092, // Kafka no TLS
		6379 // Redis
	)

	override fun getCommonPorts() : List<Int> = commonPorts
	override fun getConcurrencyLimit(): Int = concurrencyLimit
	override fun getReadTimeout(): Int = readTimeout
	override fun getConnectTimeout(): Int = connectTimeout
	override fun getMaxWaitTimeForPortScan(): Long = maxWaitTime
}