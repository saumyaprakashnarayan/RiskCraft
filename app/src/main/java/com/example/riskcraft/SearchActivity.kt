package com.example.riskcraft

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.network.ApiClient
import com.example.riskcraft.network.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private val apiKey = "YOUR_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val recyclerView = findViewById<RecyclerView>(R.id.searchRecycler)

        recyclerView.layoutManager = LinearLayoutManager(this)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length > 2) {
                    ApiClient.api.searchStocks(query, apiKey)
                        .enqueue(object : Callback<SearchResponse> {
                            override fun onResponse(
                                call: Call<SearchResponse>,
                                response: Response<SearchResponse>
                            ) {
                                val list = response.body()?.result ?: emptyList()
                                recyclerView.adapter =
                                    SearchAdapter(list) { /* open stock detail */ }
                            }

                            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {}
                        })
                }
            }
        })
    }
}
