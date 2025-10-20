package com.example.myapplication.data

import com.google.gson.annotations.SerializedName

// ============================================
// AUTENTICAÇÃO
// ============================================

data class LoginRequest(
    val usuario: String,
    val senha: String
)

data class RegisterRequest(
    val usuario: String,
    val email: String,
    val senha: String,
    @SerializedName("confirmar_senha")
    val confirmarSenha: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null
)

data class AnimeByIdResponse(
    val data: Anime
)

data class User(
    @SerializedName("id_usuario")
    val idUsuario: Int,
    val usuario: String,
    val email: String,
    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    @SerializedName("data_criacao")
    val dataCriacao: String? = null,
    val estatisticas: UserStats? = null
)

data class UserStats(
    @SerializedName("total_reviews")
    val totalReviews: Int = 0,
    @SerializedName("total_assistidos")
    val totalAssistidos: Int = 0,
    @SerializedName("total_watchlist")
    val totalWatchlist: Int = 0,
    @SerializedName("media_notas")
    val mediaNotas: String = "0.0"
)

// ============================================
// ANIMES
// ============================================

data class AnimeResponse(
    val success: Boolean,
    val data: List<Anime>,
    val pagination: Pagination? = null
)

data class Anime(
    @SerializedName("mal_id")
    val malId: Int,
    val title: String,
    @SerializedName("title_english")
    val titleEnglish: String? = null,
    val synopsis: String? = null,
    val images: AnimeImages,
    val score: Double? = null,
    val year: Int? = null,
    val episodes: Int? = null,
    val type: String? = null,  // ⬅️ ADICIONADO
    val status: String? = null
)

data class AnimeImages(
    val jpg: ImageUrls
)

data class ImageUrls(
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null
)

data class Pagination(
    @SerializedName("last_visible_page")
    val lastVisiblePage: Int,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean
)
data class UpdateReviewRequest(
    @SerializedName("id_review") val idReview: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nota") val nota: Double? = null,
    @SerializedName("texto_review") val textoReview: String? = null,
    @SerializedName("titulo_review") val tituloReview: String? = null,
    @SerializedName("data_assistido") val dataAssistido: String? = null
)

data class DeleteReviewRequest(
    @SerializedName("id_review") val idReview: Int,
    @SerializedName("id_usuario") val idUsuario: Int
)
// ============================================
// REVIEWS - ADICIONADO
// ============================================
data class Review(
    @SerializedName("ID_review")
    val idReview: Int,
    @SerializedName("ID_usuario")
    val idUsuario: Int,
    @SerializedName("ID_anime")
    val idAnime: Int,
    @SerializedName("Nota")
    val nota: Double,
    @SerializedName("Texto_review")
    val textoReview: String,
    @SerializedName("Data_criacao")
    val dataCriacao: String,
    @SerializedName("Data_atualizacao")
    val dataAtualizacao: String? = null,
    @SerializedName("Usuario")
    val usuario: String? = null,
    @SerializedName("Titulo_anime")
    val tituloAnime: String? = null,
    @SerializedName("MAL_ID")  // ← ADICIONE ESTA LINHA
    val malId: Int? = null     // ← ADICIONE ESTA LINHA
)

data class CreateReviewRequest(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("mal_id") val malId: Int,  // ← IMPORTANTE: mal_id
    @SerializedName("nota") val nota: Double,
    @SerializedName("texto_review") val textoReview: String,
    @SerializedName("titulo_review") val tituloReview: String? = null,
    @SerializedName("data_assistido") val dataAssistido: String? = null
)

data class ReviewResponse(
    val success: Boolean,
    val message: String?,
    val data: List<Review>  // ⬅️ Lista de reviews
)

// ============================================
// FAVORITOS - ADICIONADO
// ============================================

data class Favorite(
    @SerializedName("id_favorito")
    val idFavorito: Int,
    @SerializedName("id_usuario")
    val idUsuario: Int,
    @SerializedName("id_anime")
    val idAnime: Int,
    @SerializedName("data_criacao")
    val dataCriacao: String
)

data class FavoriteRequest(
    @SerializedName("id_usuario")
    val idUsuario: Int,
    @SerializedName("id_anime")
    val idAnime: Int
)

// ============================================
// LISTA DE ASSISTIDOS - ADICIONADO
// ============================================

data class WatchedAnime(
    @SerializedName("id_lista")
    val idLista: Int,
    @SerializedName("id_usuario")
    val idUsuario: Int,
    @SerializedName("id_anime")
    val idAnime: Int,
    val status: String,
    @SerializedName("data_criacao")
    val dataCriacao: String
)

data class WatchedAnimeRequest(
    @SerializedName("id_usuario")
    val idUsuario: Int,
    @SerializedName("id_anime")
    val idAnime: Int,
    val status: String = "completo"
)