package com.example.resales.Repository

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.resales.Models.SalesItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SalesRepository {
    private val baseUrl = "https://anbo-salesitems.azurewebsites.net/api/"
    private val service: SalesService

    private var allItems: List<SalesItem> = emptyList()
    val items: MutableState<List<SalesItem>> = mutableStateOf(emptyList())
    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    val errorMessage: MutableState<String> = mutableStateOf("")

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(SalesService::class.java)
        getSalesItems()
    }

    // ---------- HENT ALLE ----------
    fun getSalesItems() {
        isLoading.value = true
        errorMessage.value = ""

        service.getAllSalesItems().enqueue(object : Callback<List<SalesItem>> {
            override fun onResponse(
                call: Call<List<SalesItem>>,
                response: Response<List<SalesItem>>
            ) {
                isLoading.value = false
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    allItems = list
                    items.value = list
                    errorMessage.value = ""
                } else {
                    val msg = "HTTP ${response.code()} ${response.message()}"
                    errorMessage.value = msg
                    items.value = emptyList()
                    Log.e("SALES_REPO", msg)
                }
            }

            override fun onFailure(call: Call<List<SalesItem>>, t: Throwable) {
                isLoading.value = false
                val msg = t.message ?: "No connection to backend"
                errorMessage.value = msg
                items.value = emptyList()
                Log.e("SALES_REPO", msg)
            }
        })
    }

    // ---------- TILFØJ ITEM ----------
    fun add(item: SalesItem) {
        service.create(item).enqueue(object : Callback<SalesItem> {
            override fun onResponse(call: Call<SalesItem>, response: Response<SalesItem>) {
                if (response.isSuccessful) {
                    Log.d("SALES_REPO", "Added: ${response.body()}")
                    getSalesItems()
                    errorMessage.value = ""
                } else {
                    val message = "HTTP ${response.code()} ${response.message()}"
                    errorMessage.value = message
                    Log.e("SALES_REPO", message)
                }
            }

            override fun onFailure(call: Call<SalesItem>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessage.value = message
                Log.e("SALES_REPO", message)
            }
        })
    }

    // ---------- SLET ITEM ----------
    fun delete(id: Int) {
        Log.d("SALES_REPO", "Delete: $id")
        service.delete(id).enqueue(object : Callback<SalesItem> {
            override fun onResponse(call: Call<SalesItem>, response: Response<SalesItem>) {
                if (response.isSuccessful) {
                    Log.d("SALES_REPO", "Deleted: ${response.body()}")
                    errorMessage.value = ""
                    getSalesItems()
                } else {
                    val message = "HTTP ${response.code()} ${response.message()}"
                    errorMessage.value = message
                    Log.e("SALES_REPO", "Not deleted: $message")
                }
            }

            override fun onFailure(call: Call<SalesItem>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessage.value = message
                Log.e("SALES_REPO", "Not deleted: $message")
            }
        })
    }

    // ---------- SORTERING ----------
    fun sortByDate(ascending: Boolean) {
        Log.d("SALES_REPO", "Sort by date")
        items.value = if (ascending) {
            items.value.sortedBy { it.time }
        } else {
            items.value.sortedByDescending { it.time }
        }
    }

    fun sortByPrice(ascending: Boolean) {
        Log.d("SALES_REPO", "Sort by price")
        items.value = if (ascending) {
            items.value.sortedBy { it.price }
        } else {
            items.value.sortedByDescending { it.price }
        }
    }

    // ---------- FILTER ----------
    fun resetFilters() {
        items.value = allItems
    }

    fun filterByDescription(fragment: String) {
        val q = fragment.trim()
        if (q.isEmpty()) return
        items.value = items.value.filter { item ->
            (item.description ?: "").contains(q, ignoreCase = true)
        }
    }

    fun filterByMaxPrice(maxPrice: Int?) {
        if (maxPrice == null) return
        items.value = items.value.filter { it.price <= maxPrice }
    }
}
