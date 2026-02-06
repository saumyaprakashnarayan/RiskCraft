package com.example.riskcraft.network

data class SearchResponse(
    val count: Int,
    val result: List<SearchStock>
)
