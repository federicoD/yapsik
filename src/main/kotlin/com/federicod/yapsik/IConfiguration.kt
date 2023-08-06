package com.federicod.yapsik

interface IConfiguration {
	fun getCommonPorts() : List<Int>
	fun getConcurrencyLimit() : Int
	fun getReadTimeout() : Int
	fun getConnectTimeout() : Int
	fun getMaxWaitTimeForPortScan() : Long
}