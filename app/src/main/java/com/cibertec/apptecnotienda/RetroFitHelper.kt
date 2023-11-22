package com.cibertec.bibliotecaapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.cibertec.apptecnotienda.R


object RetroFitHelper {
    fun getRetrofitInstance(context: Context): Retrofit {
        val ipPrivada = context.getString(R.string.ipPrivada)
        val baseURL = "http://$ipPrivada:7211/"
        return Retrofit.Builder().baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}