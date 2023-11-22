package com.cibertec.apptecnotienda

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface QuoteApi {
    @GET("/getProducts")
    suspend fun getQuotes(): Response<List<Producto>>
    @GET("/GenerateJasperReport")
    fun generateJasperReport(): Call<ResponseBody>
}