package com.example.riskcraft.realtime

import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object MarketWebSocket {

    private const val BASE_URL = "wss://ws.finnhub.io?token="
    private var webSocket: WebSocket? = null

    // Live prices map (symbol -> price)
    val livePrices = ConcurrentHashMap<String, Double>()

    fun connect(apiKey: String) {
        if (webSocket != null) return

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(BASE_URL + apiKey)
            .build()

        webSocket = client.newWebSocket(request, socketListener)
    }

    fun subscribe(symbol: String) {
        val msg = JSONObject()
        msg.put("type", "subscribe")
        msg.put("symbol", symbol)
        webSocket?.send(msg.toString())
    }

    fun unsubscribe(symbol: String) {
        val msg = JSONObject()
        msg.put("type", "unsubscribe")
        msg.put("symbol", symbol)
        webSocket?.send(msg.toString())
    }

    private val socketListener = object : WebSocketListener() {

        override fun onMessage(webSocket: WebSocket, text: String) {
            val json = JSONObject(text)
            if (json.optString("type") == "trade") {
                val data = json.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val trade = data.getJSONObject(i)
                    val symbol = trade.getString("s")
                    val price = trade.getDouble("p")

                    // Update live price
                    livePrices[symbol] = price
                }
            }
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            this@MarketWebSocket.webSocket = null
        }
    }
}
