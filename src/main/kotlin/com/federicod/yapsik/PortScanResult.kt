package com.federicod.yapsik

data class PortScanResult(
    val port: Int,
    val open: Boolean,
    var fingerprintResult: ApplicationFingerprintResult? = null)