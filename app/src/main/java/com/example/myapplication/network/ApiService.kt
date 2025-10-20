package com.example.myapplication.network

import com.example.myapplication.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============================================
    // AUTENTICAÇÃO
    // ============================================
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<User>>

    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>

    // ============================================
    // ANIMES
    // ============================================
    @GET("animes/search.php")
    suspend fun getPopularAnimes(
        @Query("popular") popular: Boolean = true,
        @Query("page") page: Int = 1
    ): Response<AnimeResponse>

    @GET("animes/search.php")
    suspend fun getTopAnimes(
        @Query("top") top: Boolean = true,
        @Query("page") page: Int = 1
    ): Response<AnimeResponse>

    @GET("animes/search.php")
    suspend fun searchAnimes(
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): Response<AnimeResponse>

    // ============================================
    // REVIEWS - CORRIGIDO
    // ============================================
    @GET("reviews/manage.php")
    suspend fun getReviews(
        @Query("id_usuario") idUsuario: Int? = null,
        @Query("id_anime") idAnime: Int? = null,
        @Query("limit") limit: Int = 100
    ): Response<ReviewResponse>

    @POST("reviews/manage.php")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<Review>>

    @PUT("reviews/manage.php")
    suspend fun updateReview(@Body request: UpdateReviewRequest): Response<ApiResponse<Review>>

    @HTTP(method = "DELETE", path = "reviews/manage.php", hasBody = true)
    suspend fun deleteReview(@Body request: DeleteReviewRequest): Response<ApiResponse<Unit>>

    // ============================================
    // FAVORITOS - ADICIONADO
    // ============================================
    @GET("favorites/list.php")
    suspend fun getFavorites(
        @Query("id_usuario") idUsuario: Int
    ): Response<ApiResponse<List<Favorite>>>

    @POST("favorites/add.php")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<ApiResponse<Favorite>>

    @DELETE("favorites/remove.php")
    suspend fun removeFavorite(
        @Query("id_usuario") idUsuario: Int,
        @Query("id_anime") idAnime: Int
    ): Response<ApiResponse<Nothing>>

    // ============================================
    // LISTA DE ASSISTIDOS - ADICIONADO
    // ============================================
    @GET("watchlist/list.php")
    suspend fun getWatchedAnimes(
        @Query("id_usuario") idUsuario: Int
    ): Response<ApiResponse<List<WatchedAnime>>>

    @POST("watchlist/add.php")
    suspend fun addToWatchlist(@Body request: WatchedAnimeRequest): Response<ApiResponse<WatchedAnime>>

    @DELETE("watchlist/remove.php")
    suspend fun removeFromWatchlist(
        @Query("id_usuario") idUsuario: Int,
        @Query("id_anime") idAnime: Int
    ): Response<ApiResponse<Nothing>>
}