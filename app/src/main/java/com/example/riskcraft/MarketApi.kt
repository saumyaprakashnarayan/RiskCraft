package com.example.riskcraft.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {

    @GET("quote")
    fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): Call<MarketResponse>

    @GET("search")
    fun searchStocks(
        @Query("q") query: String,
        @Query("token") token: String
    ): Call<SearchResponse>
}
