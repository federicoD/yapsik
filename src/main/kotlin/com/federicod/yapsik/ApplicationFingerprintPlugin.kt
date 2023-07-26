package com.federicod.yapsik

import java.net.Socket

interface ApplicationFingerprintPlugin {
    fun isPortAccepted(port: Int) : Boolean

    fun run(socket: Socket) : ApplicationFingerprintResult
}