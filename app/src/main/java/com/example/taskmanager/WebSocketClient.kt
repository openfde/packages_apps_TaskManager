package com.example.taskmanager

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.ktor.client.engine.cio.*
import io.ktor.client.request.get

interface WebSocketListener {
    fun onConnected()
    fun onMessage(message: String)
    fun onDisconnected()
}

class DaemonClient(
    private val host: String,
    private val port: Int,
    private val listener: WebSocketListener
) {

    private val client = HttpClient(CIO) {
        install(WebSockets)


        engine {
            requestTimeout = 1000 // 请求超时，单位为毫秒
            endpoint {
                connectTimeout = 5000  // 连接超时
                socketTimeout = 10000  // 套接字超时（读写）
            }
        }
    }

    fun connectProcessInfoSocket(scope: CoroutineScope) {
        scope.launch {
            try {
                client.ws(
                    method = HttpMethod.Get,
                    host = host,
                    port = port,
                    path = "/process"
                ) {
                    listener.onConnected()
                    try {
                        for (frame in incoming) {
                            if (frame !is Frame.Text) continue
                            listener.onMessage(frame.toString())
                        }
                    } catch (e: Exception) {
                        listener.onDisconnected()
                    }
                }
            } catch (e: Exception) {
                Log.d("Websocket", "Error")
            }
        }
    }

    fun destroy() {
        // if destroy is called,
        // then the socket will
        // be closed forever
        client.close()
    }

    fun getIcon(scope: CoroutineScope,iconUrl: String) {
        scope.launch {
            try {
                val result = client.get<ByteArray> {
                }
            } catch (e: Exception) {

            }
        }
    }

}