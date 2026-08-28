package com.example.util

import android.content.Context
import com.example.data.repository.ShipmentRepository
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.html.*
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface

class LocalServerManager(
    private val context: Context,
    private val repository: ShipmentRepository
) {
    private var server: NettyApplicationEngine? = null

    fun startServer(port: Int = 8080) {
        if (server != null) return

        server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/") {
                    val shipments = repository.allShipments.first()
                    call.respondHtml {
                        head {
                            title { +"Cargo Shipments Monitor" }
                            style {
                                +"""
                                    body { font-family: Tahoma, sans-serif; direction: rtl; background: #f4f4f9; padding: 20px; }
                                    .card { background: white; border-radius: 8px; padding: 15px; margin-bottom: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                                    .header { color: #1a73e8; border-bottom: 2px solid #1a73e8; padding-bottom: 10px; }
                                    img { max-width: 200px; border-radius: 4px; }
                                """.trimIndent()
                            }
                        }
                        body {
                            h1(classes = "header") { +"لیست محموله‌های باری (شبکه محلی)" }
                            shipments.forEach { shipment ->
                                div(classes = "card") {
                                    h3 { +shipment.cargoDescription }
                                    p { +"فرستنده: ${shipment.senderName} | گیرنده: ${shipment.receiverName}" }
                                    p { +"تاریخ: ${shipment.jalaliYear}/${shipment.jalaliMonth}/${shipment.jalaliDay}" }
                                    if (shipment.imagePath != null) {
                                        val file = File(shipment.imagePath)
                                        if (file.exists()) {
                                            p { +"تصویر بار موجود است (فقط روی دستگاه اصلی قابل مشاهده)" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = false)
    }

    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is InetAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr.indexOf(':') < 0) return sAddr
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}
