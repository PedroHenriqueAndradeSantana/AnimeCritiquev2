package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.*
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

// Cores
val MainBackgroundColor = Color(0xFF1A1A1A)
val YellowButtonColor = Color(0xFFFFC700)
val GrayInputColor = Color(0xFFC4C4C4)
val CardBackgroundColor = Color(0xFF2A2A2A)
val TextPrimaryColor = Color.White
val TextSecondaryColor = Color(0xFFB0B0B0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

// ============================================
// EDIT REVIEW SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReviewScreen(
    reviewId: Int,
    animeId: Int,
    user: User,
    navController: NavHostController
) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var currentReview by remember { mutableStateOf<Review?>(null) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(reviewId) {
        scope.launch {
            try {
                // Buscar review atual
                val reviewResponse = RetrofitClient.apiService.getReviews(idUsuario = user.idUsuario)
                if (reviewResponse.isSuccessful) {
                    currentReview = reviewResponse.body()?.data?.find { it.idReview == reviewId }
                    currentReview?.let { review ->
                        rating = review.nota.toInt()
                        reviewText = review.textoReview

                        // Buscar anime usando o MAL_ID da review
                        review.malId?.let { malId ->
                            val animeResponse = RetrofitClient.jikanApiService.getAnimeById(malId)
                            if (animeResponse.isSuccessful) {
                                anime = animeResponse.body()?.data
                            }
                        }
                    }
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro ao carregar", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Editar Avaliação", color = Color.White, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowButtonColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                anime?.let { animeData ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier
                                .width(80.dp)
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(animeData.images.jpg.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = animeData.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = animeData.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Sua Nota",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Estrela ${index + 1}",
                                    modifier = Modifier.size(40.dp),
                                    tint = YellowButtonColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Seu Comentário",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = { Text("Escreva sua avaliação (mínimo 10 caracteres)", color = TextSecondaryColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = YellowButtonColor
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                rating == 0 -> {
                                    Toast.makeText(context, "⭐ Selecione uma nota", Toast.LENGTH_SHORT).show()
                                }
                                reviewText.trim().length < 10 -> {
                                    Toast.makeText(context, "📝 O comentário deve ter no mínimo 10 caracteres", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    isSaving = true
                                    scope.launch {
                                        try {
                                            val request = UpdateReviewRequest(
                                                idReview = reviewId,
                                                idUsuario = user.idUsuario,
                                                nota = rating.toDouble(),
                                                textoReview = reviewText.trim()
                                            )

                                            val response = RetrofitClient.apiService.updateReview(request)

                                            if (response.isSuccessful && response.body()?.success == true) {
                                                Toast.makeText(context, "✅ Avaliação atualizada!", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            } else {
                                                Toast.makeText(context, "❌ Erro ao atualizar", Toast.LENGTH_SHORT).show()
                                            }
                                            isSaving = false
                                        } catch (e: Exception) {
                                            isSaving = false
                                            Toast.makeText(context, "❌ Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowButtonColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            currentUser?.let { user ->
                HomeScreenWithDrawer(
                    user = user,
                    navController = navController
                )
            }
        }

        composable("myReviews") {
            currentUser?.let { user ->
                MyReviewsScreen(
                    user = user,
                    navController = navController
                )
            }
        }

        composable("searchAnimes") {
            currentUser?.let { user ->
                SearchAnimesScreen(
                    user = user,
                    navController = navController
                )
            }
        }

        composable("myList") {
            currentUser?.let { user ->
                MyListScreen(
                    user = user,
                    navController = navController
                )
            }
        }

        composable("favorites") {
            currentUser?.let { user ->
                FavoritesScreen(
                    user = user,
                    navController = navController
                )
            }
        }

        composable(
            route = "animeDetail/{animeId}",
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
            currentUser?.let { user ->
                AnimeDetailScreen(
                    animeId = animeId,
                    user = user,
                    navController = navController
                )
            }
        }

        composable(
            route = "createReview/{animeId}",
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
            currentUser?.let { user ->
                CreateReviewScreen(
                    animeId = animeId,
                    user = user,
                    navController = navController
                )
            }
        }

        composable(
            route = "editReview/{reviewId}/{animeId}",
            arguments = listOf(
                navArgument("reviewId") { type = NavType.IntType },
                navArgument("animeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getInt("reviewId") ?: 0
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
            currentUser?.let { user ->
                EditReviewScreen(
                    reviewId = reviewId,
                    animeId = animeId,
                    user = user,
                    navController = navController
                )
            }
        }
    }
}

// ============================================
// LOGIN SCREEN
// ============================================
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundColor)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_animecritique),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.banner_anime_critique),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 24.dp)
            )

            Text("Faça login para continuar", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuário") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.login(
                                LoginRequest(username.trim(), password)
                            )

                            isLoading = false

                            if (response.isSuccessful && response.body()?.success == true) {
                                val user = response.body()?.data
                                if (user != null) {
                                    Toast.makeText(context, "Bem-vindo, ${user.usuario}!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(user)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    response.body()?.message ?: "Erro ao fazer login",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowButtonColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", color = Color.Black, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRegisterClick) {
                Text("Não tem conta? Registre-se", color = YellowButtonColor)
            }
        }
    }
}

// ============================================
// REGISTER SCREEN
// ============================================
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundColor)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_animecritique),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text("Registrar usuário", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuário") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YellowButtonColor,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor,
                    focusedLeadingIconColor = YellowButtonColor,
                    unfocusedLeadingIconColor = GrayInputColor,
                    focusedLabelColor = YellowButtonColor,
                    unfocusedLabelColor = GrayInputColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() -> {
                            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            Toast.makeText(context, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                        }
                        password.length < 6 -> {
                            Toast.makeText(context, "Senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.register(
                                        RegisterRequest(
                                            usuario = username.trim(),
                                            email = email.trim(),
                                            senha = password,
                                            confirmarSenha = confirmPassword
                                        )
                                    )

                                    isLoading = false

                                    if (response.isSuccessful && response.body()?.success == true) {
                                        Toast.makeText(context, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                                        onRegisterSuccess()
                                    } else {
                                        val errors = response.body()?.errors?.joinToString("\n")
                                            ?: response.body()?.message
                                            ?: "Erro ao cadastrar"
                                        Toast.makeText(context, errors, Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowButtonColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrar", color = Color.Black, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Já tem conta? Faça login", color = YellowButtonColor)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ============================================
// DRAWER (SIDEBAR)
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    user: User,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackgroundColor)
            .padding(top = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.usuario,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryColor
            )

            Text(
                text = "@${user.usuario.lowercase()}",
                fontSize = 14.sp,
                color = TextSecondaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimaryColor
                    )
                ) {
                    Text("500 Followers", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimaryColor
                    )
                ) {
                    Text("420 Followings", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider(color = Color.Gray)

        DrawerMenuItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )

        DrawerMenuItem(
            icon = Icons.Default.DateRange,
            label = "Avaliações",
            isSelected = currentRoute == "myReviews",
            onClick = { onNavigate("myReviews") }
        )

        DrawerMenuItem(
            icon = Icons.Default.Search,
            label = "Animes",
            isSelected = currentRoute == "searchAnimes",
            onClick = { onNavigate("searchAnimes") }
        )

        DrawerMenuItem(
            icon = Icons.Default.List,
            label = "Minha lista de anime",
            isSelected = currentRoute == "myList",
            onClick = { onNavigate("myList") }
        )

        DrawerMenuItem(
            icon = Icons.Default.Favorite,
            label = "Curtidas",
            isSelected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = Color.Gray)

        DrawerMenuItem(
            icon = Icons.Default.ExitToApp,
            label = "Logout",
            isSelected = false,
            onClick = onLogout
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) YellowButtonColor.copy(alpha = 0.3f) else Color.Transparent
    val iconColor = if (isSelected) YellowButtonColor else TextSecondaryColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = iconColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ============================================
// HOME SCREEN WITH DRAWER
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWithDrawer(
    user: User,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = CardBackgroundColor
            ) {
                DrawerContent(
                    user = user,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                        scope.launch { drawerState.close() }

                        when (route) {
                            "home" -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            "myReviews" -> navController.navigate("myReviews")
                            "searchAnimes" -> navController.navigate("searchAnimes")
                            "myList" -> navController.navigate("myList")
                            "favorites" -> navController.navigate("favorites")
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) {
        HomeScreen(
            user = user,
            navController = navController,
            onMenuClick = {
                scope.launch {
                    drawerState.open()
                }
            }
        )
    }
}

// ============================================
// HOME SCREEN
// ============================================
@Composable
fun HomeScreen(
    user: User,
    navController: NavHostController,
    onMenuClick: () -> Unit = {}
) {
    var popularAnimes by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var topAnimes by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val popularResponse = RetrofitClient.jikanApiService.getTopAnimes(page = 1, limit = 25)
                if (popularResponse.isSuccessful) {
                    popularAnimes = popularResponse.body()?.data ?: emptyList()
                }

                val topResponse = RetrofitClient.jikanApiService.getTopAnimes(page = 1, limit = 25)
                if (topResponse.isSuccessful) {
                    topAnimes = topResponse.body()?.data ?: emptyList()
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro ao carregar animes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(containerColor = MainBackgroundColor) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowButtonColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .safeDrawingPadding()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Hello, ${user.usuario}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            "Escolha e opine sobre seus animes favoritos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    AnimeCarousel(title = "Animes Populares", animes = popularAnimes, navController = navController)
                }

                item {
                    AnimeCarousel(title = "Top Animes", animes = topAnimes, navController = navController)
                }
            }
        }
    }
}

@Composable
fun AnimeCarousel(title: String, animes: List<Anime>, navController: NavHostController) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animes) { anime ->
                AnimePosterCard(anime = anime, navController = navController)
            }
        }
    }
}

@Composable
fun AnimePosterCard(anime: Anime, navController: NavHostController) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(210.dp)
            .clickable { navController.navigate("animeDetail/${anime.malId}") },
        shape = RoundedCornerShape(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(anime.images.jpg.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = anime.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ============================================
// MY REVIEWS SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    user: User,
    navController: NavHostController
) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Review?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getReviews(idUsuario = user.idUsuario)
                if (response.isSuccessful) {
                    reviews = response.body()?.data ?: emptyList()
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Minhas Avaliações", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowButtonColor)
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Você ainda não fez nenhuma avaliação",
                        color = TextSecondaryColor
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    MyReviewCard(
                        review = review,
                        onEdit = {
                            navController.navigate("editReview/${review.idReview}/${review.idAnime}")
                        },
                        onDelete = {
                            showDeleteDialog = review
                        }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { review ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = CardBackgroundColor,
            title = {
                Text("Excluir Avaliação", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Tem certeza que deseja excluir esta avaliação? Esta ação não pode ser desfeita.",
                    color = TextSecondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val deleteRequest = DeleteReviewRequest(
                                    idReview = review.idReview,
                                    idUsuario = user.idUsuario
                                )
                                val response = RetrofitClient.apiService.deleteReview(deleteRequest)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "✅ Avaliação excluída!", Toast.LENGTH_SHORT).show()
                                    reviews = reviews.filter { it.idReview != review.idReview }
                                } else {
                                    Toast.makeText(context, "❌ Erro ao excluir", Toast.LENGTH_SHORT).show()
                                }
                                showDeleteDialog = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ Erro: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondaryColor)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MyReviewCard(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.tituloAnime ?: "Anime #${review.idAnime}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.nota.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = YellowButtonColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${review.nota.toInt()}/5",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowButtonColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextSecondaryColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = review.dataCriacao,
                            fontSize = 13.sp,
                            color = TextSecondaryColor
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .background(YellowButtonColor.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = YellowButtonColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = TextSecondaryColor.copy(alpha = 0.2f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = review.textoReview,
                fontSize = 15.sp,
                color = Color.White,
                lineHeight = 22.sp
            )
        }
    }
}

// ============================================
// SEARCH ANIMES SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAnimesScreen(
    user: User,
    navController: NavHostController
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Buscar Animes", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite o nome do anime...", color = TextSecondaryColor) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = YellowButtonColor)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = YellowButtonColor,
                    unfocusedBorderColor = GrayInputColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.searchAnimes(searchQuery)
                                if (response.isSuccessful) {
                                    searchResults = response.body()?.data ?: emptyList()
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowButtonColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YellowButtonColor)
                }
            } else if (searchResults.isEmpty() && searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondaryColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pesquise por seus animes favoritos",
                            color = TextSecondaryColor,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum anime encontrado",
                        color = TextSecondaryColor
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { anime ->
                        SearchResultCard(anime = anime, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(anime: Anime, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("animeDetail/${anime.malId}") },
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.images.jpg.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = YellowButtonColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = anime.score?.toString() ?: "N/A",
                        fontSize = 14.sp,
                        color = TextSecondaryColor
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = anime.type ?: "TV",
                        fontSize = 12.sp,
                        color = TextSecondaryColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Episódios: ${anime.episodes ?: "?"}",
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalhes",
                tint = TextSecondaryColor
            )
        }
    }
}

// ============================================
// MY LIST SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListScreen(
    user: User,
    navController: NavHostController
) {
    var watchedAnimes by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                watchedAnimes = emptyList()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Minha Lista de Animes", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowButtonColor)
            }
        } else if (watchedAnimes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sua lista está vazia",
                        color = TextSecondaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adicione animes que você já assistiu",
                        color = TextSecondaryColor,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchedAnimes) { anime ->
                    WatchedAnimeCard(
                        anime = anime,
                        onRemove = {
                            scope.launch {
                                try {
                                    Toast.makeText(context, "Removido da lista!", Toast.LENGTH_SHORT).show()
                                    watchedAnimes = watchedAnimes.filter { it.malId != anime.malId }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchedAnimeCard(
    anime: Anime,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.images.jpg.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assistido",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${anime.episodes ?: "?"} episódios",
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = Color.Red
                )
            }
        }
    }
}

// ============================================
// FAVORITES SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    user: User,
    navController: NavHostController
) {
    var favoriteAnimes by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                favoriteAnimes = emptyList()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Animes Favoritos", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowButtonColor)
            }
        } else if (favoriteAnimes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum favorito ainda",
                        color = TextSecondaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Favorite seus animes preferidos!",
                        color = TextSecondaryColor,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteAnimes) { anime ->
                    FavoriteAnimeCard(
                        anime = anime,
                        navController = navController,
                        onUnfavorite = {
                            scope.launch {
                                try {
                                    Toast.makeText(context, "Removido dos favoritos!", Toast.LENGTH_SHORT).show()
                                    favoriteAnimes = favoriteAnimes.filter { it.malId != anime.malId }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteAnimeCard(
    anime: Anime,
    navController: NavHostController,
    onUnfavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("animeDetail/${anime.malId}") },
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.images.jpg.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = YellowButtonColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = anime.score?.toString() ?: "N/A",
                        fontSize = 14.sp,
                        color = TextSecondaryColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${anime.episodes ?: "?"} episódios",
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }

            IconButton(onClick = onUnfavorite) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Desfavoritar",
                    tint = Color.Red
                )
            }
        }
    }
}

// ============================================
// ANIME DETAIL SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    animeId: Int,
    user: User,
    navController: NavHostController
) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }
    var isInList by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(animeId) {
        scope.launch {
            try {
                // Buscar anime específico pela API do Jikan
                val animeResponse = RetrofitClient.jikanApiService.getAnimeById(animeId)
                if (animeResponse.isSuccessful) {
                    anime = animeResponse.body()?.data
                }

                val reviewsResponse = RetrofitClient.apiService.getReviews(idAnime = animeId)
                if (reviewsResponse.isSuccessful) {
                    reviews = reviewsResponse.body()?.data ?: emptyList()

                    if (reviews.isNotEmpty()) {
                        averageRating = reviews.map { it.nota }.average()
                    }

                    reviews = reviews.sortedByDescending { it.nota }
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Erro ao carregar anime: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = YellowButtonColor)
        }
    } else {
        anime?.let { animeData ->
            Scaffold(
                containerColor = MainBackgroundColor
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(animeData.images.jpg.largeImageUrl ?: animeData.images.jpg.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = animeData.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MainBackgroundColor
                                            )
                                        )
                                    )
                            )

                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(animeData.images.jpg.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = animeData.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = animeData.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${animeData.year ?: "N/A"} | ${animeData.episodes ?: "?"} eps",
                                    fontSize = 14.sp,
                                    color = TextSecondaryColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Type: ${animeData.type ?: "TV"}",
                                    fontSize = 14.sp,
                                    color = TextSecondaryColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Status: ${animeData.status ?: "Unknown"}",
                                    fontSize = 14.sp,
                                    color = TextSecondaryColor
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "SYNOPSIS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = animeData.synopsis ?: "No synopsis available.",
                                fontSize = 14.sp,
                                color = TextSecondaryColor,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Ratings",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format("%.1f", if (reviews.isNotEmpty()) averageRating else (animeData.score ?: 0.0)),
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6B6B)
                                    )

                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (index < (if (reviews.isNotEmpty()) averageRating else (animeData.score ?: 0.0)).toInt())
                                                    Color(0xFFFF6B6B) else TextSecondaryColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${reviews.size} reviews",
                                        fontSize = 12.sp,
                                        color = TextSecondaryColor
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("createReview/${animeData.malId}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB3BA)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Avaliar e Comentar", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    isInList = !isInList
                                    scope.launch {
                                        try {
                                            if (isInList) {
                                                Toast.makeText(context, "Adicionado à sua lista!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Removido da sua lista!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInList) Color(0xFF4CAF50) else Color(0xFFFFB3BA)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (isInList) Icons.Default.CheckCircle else Icons.Default.List,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isInList) "Na Minha Lista" else "Adicionar à Minha Lista",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    isFavorite = !isFavorite
                                    scope.launch {
                                        try {
                                            if (isFavorite) {
                                                Toast.makeText(context, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Removido dos favoritos!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFavorite) Color.Red else Color(0xFFFFB3BA)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isFavorite) "Favoritado" else "Favoritar",
                                    color = if (isFavorite) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Reviews",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (reviews.isNotEmpty()) {
                                TextButton(onClick = { }) {
                                    Text("See all", color = Color(0xFFFF6B6B))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    if (reviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma avaliação ainda. Seja o primeiro!",
                                    color = TextSecondaryColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(reviews.take(3)) { review ->
                            ReviewCard(review = review)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = review.usuario ?: "Usuário",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(review.nota.toInt()) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFF6B6B)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = review.dataCriacao,
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            var expanded by remember { mutableStateOf(false) }

            Text(
                text = review.textoReview,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            if (review.textoReview.length > 150) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Read less" else "Read more >",
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B)
                    )
                }
            }
        }
    }
}

// ============================================
// CREATE REVIEW SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    animeId: Int,
    user: User,
    navController: NavHostController
) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("06 March 2022") }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(animeId) {
        scope.launch {
            try {
                // Buscar anime específico pela API do Jikan
                val animeResponse = RetrofitClient.jikanApiService.getAnimeById(animeId)
                if (animeResponse.isSuccessful) {
                    anime = animeResponse.body()?.data
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar anime: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MainBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Escreva aqui a sua avaliação", color = Color.White, fontSize = 16.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MainBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            anime?.let { animeData ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = animeData.title.lowercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Specify the date you watched it",
                            fontSize = 12.sp,
                            color = TextSecondaryColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(CardBackgroundColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = TextSecondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedDate,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(onClick = { }) {
                                Text("Change", color = YellowButtonColor, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(animeData.images.jpg.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = animeData.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Estrela ${index + 1}",
                                    modifier = Modifier.size(40.dp),
                                    tint = YellowButtonColor
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritar",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        modifier = Modifier.fillMaxSize(),
                        placeholder = { Text("Escreva sua avaliação", color = TextSecondaryColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = YellowButtonColor
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                            when {
                                rating == 0 -> {
                                    Toast.makeText(context, "Selecione uma nota", Toast.LENGTH_SHORT).show()
                                }
                                reviewText.isEmpty() -> {
                                    Toast.makeText(context, "Escreva um comentário", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val request = CreateReviewRequest(
                                                idUsuario = user.idUsuario,
                                                malId = animeId,  // Usando malId agora
                                                nota = rating.toDouble(),
                                                textoReview = reviewText
                                            )

                                            val response = RetrofitClient.apiService.createReview(request)

                                            if (response.isSuccessful && response.body()?.success == true) {
                                                Toast.makeText(context, "Avaliação publicada!", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            } else {
                                                val errorBody = response.errorBody()?.string()
                                                val errorMsg = """
                                                    Erro ao publicar!
                                                    Código: ${response.code()}
                                                    Mensagem: ${response.body()?.message ?: "N/A"}
                                                    Erro: ${errorBody ?: "Nenhum"}
                                                """.trimIndent()

                                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                android.util.Log.e("CreateReview", errorMsg)
                                            }
                                            isLoading = false
                                        } catch (e: Exception) {
                                            isLoading = false
                                            val errorMsg = """
                                                Exception!
                                                Tipo: ${e.javaClass.simpleName}
                                                Mensagem: ${e.message}
                                            """.trimIndent()

                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                            android.util.Log.e("CreateReview", "Exception: ${e.message}", e)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .width(140.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowButtonColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black
                            )
                        } else {
                            Text("Publish", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}