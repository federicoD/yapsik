package com.federicod.yapsik

data class ApplicationFingerprintResult(
    val gotValidResponse: Boolean,
    val applicationType: String,
    val banner: String = "",
    val version: String = ""
)