package com.example.myapplication.network

import com.example.myapplication.data.*
import retrofit2.Response
import retrofit2.http.*

interface JikanApiService {

    @GET("anime/{id}")
    suspend fun getAnimeById(@Path("id") animeId: Int): Response<AnimeByIdResponse>

    @GET("top/anime")
    suspend fun getTopAnimes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): Response<AnimeResponse>

    @GET("anime")
    suspend fun searchAnimes(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): Response<AnimeResponse>
}