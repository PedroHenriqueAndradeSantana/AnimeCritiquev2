// Arquivo: ApiService.kt
package com.example.myapplication.network

import com.example.myapplication.data.LoginResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {



    @GET("login.php")
    fun login(
        @Query("usuario") usuario: String,
        @Query("senha") senha: String
    ): Call<List<LoginResponse>>
}