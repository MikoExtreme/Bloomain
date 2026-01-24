package pt.ipt.bloomain

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditCredentialsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_credentials)

        val userId = intent.getStringExtra("USER_ID") ?: ""

        val editNameField = findViewById<EditText>(R.id.editNewUsername)

        // 1. Pega o nome que veio na "viagem" da Intent
        val currentName = intent.getStringExtra("CURRENT_USERNAME")

        if (!currentName.isNullOrEmpty()) {
            editNameField.setText(currentName)
            // Coloca o cursor no fim do texto
            editNameField.setSelection(editNameField.text.length)
        }

        findViewById<Button>(R.id.btnSaveCredentials).setOnClickListener {
            val newUsername = findViewById<EditText>(R.id.editNewUsername).text.toString().trim()
            val newPassword = findViewById<EditText>(R.id.editNewPassword).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.editConfirmPassword).text.toString().trim()
            val newBio = findViewById<EditText>(R.id.editBio).text.toString().trim()

            // 1. Verifica se não está tudo vazio
            if (newUsername.isEmpty() && newPassword.isEmpty() && newBio.isEmpty()) {
                Toast.makeText(this, "Preencha pelo menos um campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Para aqui a execução
            }

            // 2. Se o utilizador escreveu uma password, temos de validar
            if (newPassword.isNotEmpty()) {
                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "As palavras-passe não coincidem!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Para aqui a execução e não envia nada
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "A palavra-passe deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // 3. SÓ AGORA é que enviamos para o servidor, porque passou todos os testes
            updateUserInServer(userId, newUsername, newPassword, newBio)
        }
    }

    private fun updateUserInServer(id: String, username: String, password: String, bio: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Criamos o mapa dinâmico para enviar apenas o que foi alterado
        val updateData = mutableMapOf<String, String>()
        if (username.isNotEmpty()) updateData["username"] = username
        if (password.isNotEmpty()) updateData["password"] = password
        if (bio.isNotEmpty()) updateData["bio"] = bio

        apiService.updateUser(id, updateData).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditCredentialsActivity, "✅ Alterado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha e volta às definições
                } else {
                    Toast.makeText(this@EditCredentialsActivity, "❌ Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@EditCredentialsActivity, "🌐 Falha na ligação", Toast.LENGTH_SHORT).show()
            }
        })
    }
}