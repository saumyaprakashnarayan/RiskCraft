package com.example.riskcraft

data class MarketStock(
    val symbol: String,
    val name: String,
    val exchange: String,
    val price: Double,
    val changePercent: Double,
    val sector: String
)

object IndianMarketData {

    private val nseStocks = mapOf(
        "RELIANCE" to Pair("Reliance Industries Ltd", 2945.00),
        "TCS" to Pair("Tata Consultancy Services", 4120.50),
        "HDFCBANK" to Pair("HDFC Bank Ltd", 1680.20),
        "INFY" to Pair("Infosys Limited", 1525.75),
        "ITC" to Pair("ITC Limited", 465.30),
        "SBIN" to Pair("State Bank of India", 785.60),
        "BHARTIARTL" to Pair("Bharti Airtel Ltd", 1560.40),
        "KOTAKBANK" to Pair("Kotak Mahindra Bank", 1890.15),
        "LT" to Pair("Larsen & Toubro Ltd", 3450.80),
        "WIPRO" to Pair("Wipro Limited", 475.25),
        "TATAMOTORS" to Pair("Tata Motors Ltd", 785.40),
        "ADANIENT" to Pair("Adani Enterprises Ltd", 2680.90),
        "ICICIBANK" to Pair("ICICI Bank Ltd", 1245.60),
        "MARUTI" to Pair("Maruti Suzuki India Ltd", 12450.30),
        "SUNPHARMA" to Pair("Sun Pharma Industries Ltd", 1780.50)
    )

    private val indexData = mapOf(
        "NIFTY 50" to 24850.45,
        "SENSEX" to 81750.20,
        "BANK NIFTY" to 52400.75,
        "NIFTY IT" to 38250.60,
        "NIFTY PHARMA" to 19800.30
    )

    fun getIndianStocks(): List<MarketStock> {
        return nseStocks.map { (symbol, data) ->
            val change = (Math.random() * 6 - 3)
            val currentPrice = data.second * (1 + change / 100)
            MarketStock(
                symbol, data.first, "NSE",
                String.format("%.2f", currentPrice).toDouble(),
                String.format("%.2f", change).toDouble(),
                "Equity"
            )
        }
    }

    fun getIndices(): List<MarketStock> {
        return indexData.map { (name, basePrice) ->
            val change = (Math.random() * 4 - 2)
            val currentPrice = basePrice * (1 + change / 100)
            MarketStock(
                name, name, "NSE",
                String.format("%.2f", currentPrice).toDouble(),
                String.format("%.2f", change).toDouble(),
                "Index"
            )
        }
    }

    fun getCryptoSymbols(): List<Pair<String, String>> {
        return listOf(
            "BINANCE:BTCUSDT" to "Bitcoin (BTC)",
            "BINANCE:ETHUSDT" to "Ethereum (ETH)",
            "BINANCE:SOLUSDT" to "Solana (SOL)",
            "BINANCE:BNBUSDT" to "BNB",
            "BINANCE:XRPUSDT" to "Ripple (XRP)",
            "BINANCE:DOGEUSDT" to "Dogecoin (DOGE)"
        )
    }

    fun getStockPrice(symbol: String): Double {
        val base = nseStocks[symbol]?.second ?: indexData[symbol] ?: 1000.0
        val change = (Math.random() * 4 - 2)
        return String.format("%.2f", base * (1 + change / 100)).toDouble()
    }

    fun getStockName(symbol: String): String {
        return nseStocks[symbol]?.first ?: if (indexData.containsKey(symbol)) symbol else symbol
    }

    fun isIndex(symbol: String): Boolean {
        return indexData.containsKey(symbol)
    }

    fun getBasePrice(symbol: String): Double {
        return nseStocks[symbol]?.second ?: indexData[symbol] ?: 1000.0
    }

    fun getAllStockSymbols(): List<Pair<String, String>> {
        return nseStocks.map { (symbol, data) -> symbol to data.first }
    }
}
