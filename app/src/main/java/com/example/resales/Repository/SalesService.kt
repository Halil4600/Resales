package com.example.resales.Repository

import com.example.resales.Models.SalesItem
import retrofit2.Call
import retrofit2.http.*

interface SalesService {
    @GET("SalesItems")
    fun getAllSalesItems(): Call<List<SalesItem>>

    @POST("SalesItems")
    fun create(@Body item: SalesItem): Call<SalesItem>

    @DELETE("SalesItems/{id}")
    fun delete(@Path("id") id: Int): Call<SalesItem>
}
