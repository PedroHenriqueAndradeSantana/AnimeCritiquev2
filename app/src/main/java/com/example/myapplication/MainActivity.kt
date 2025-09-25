package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.LoginResponse
import com.example.myapplication.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var editUser: EditText
    private lateinit var editPassword: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        editUser = findViewById(R.id.editUser)
        editPassword = findViewById(R.id.editPassword)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {

        val usuario = editUser.text.toString().trim()
        val senha = editPassword.text.toString().trim()

        if (usuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o usuário e a senha", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = Retrofit.Builder()
            //lembrar de trocar o ip usando o ipconfig no cmd
            .baseUrl("http://10.135.138.14/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.login(usuario, senha)

        call.enqueue(object:Callback<List<LoginResponse>> {
            override fun onResponse(call: Call<List<LoginResponse>>, response: Response<List<LoginResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponses = response.body()!!

                    if (loginResponses.isNotEmpty()) {

                        val dadosDoUsuario = loginResponses[0]
                        Toast.makeText(this@MainActivity, "Bem-vindo, ${dadosDoUsuario.usuarioNome}!", Toast.LENGTH_LONG).show()

                         val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Erro na resposta do servidor", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<LoginResponse>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}


