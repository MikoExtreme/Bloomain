package pt.ipt.bloomain

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.bloomain.User // Garante que este é o caminho do teu model User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingsActivity : AppCompatActivity() {

    private var base64Image: String = ""

    // Seletor de Imagem (Igual ao do teu Registo)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                // Converte para Base64
                base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                // Envia logo para o servidor
                updateProfileImage(base64Image)
            }
        }
    }

    val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.1.211:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Certifica-te que o nome do layout é 'activity_settings' (o ficheiro que criámos)
        setContentView(R.layout.settings)

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val currentUserName = intent.getStringExtra("CURRENT_USERNAME") ?: ""

        // 1. IR PARA EDITAR PERFIL (Nome, Bio e Password)
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditCredentialsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("CURRENT_USERNAME", currentUserName)
            startActivity(intent)
        }

        // 2. IR PARA EDITAR IMAGEM
        findViewById<Button>(R.id.item_section_image).setOnClickListener {
            val intent = Intent(this, EditImageActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // 3. VOLTAR ATRÁS
        findViewById<ImageButton>(R.id.go_back).setOnClickListener {
            finish()
        }

        // 4. LOGOUT (Limpa o histórico e volta ao Login)
        findViewById<Button>(R.id.log_out).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 1. Localizar o botão
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        btnDeleteAccount.setOnClickListener {
            // Cria o Alerta de Confirmação
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Atenção!")
            builder.setMessage("Tens a certeza que queres eliminar a tua conta? Todos os teus posts e comentários serão apagados permanentemente.")

            // Botão Positivo (Apagar)
            builder.setPositiveButton("Sim, Eliminar") { _, _ ->
                val userId = intent.getStringExtra("USER_ID") ?: ""

                // Chamada à API apenas se o utilizador confirmar
                apiService.deleteAccount(userId).enqueue(object : Callback<PostResponse> {
                    override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SettingsActivity, "Conta eliminada com sucesso", Toast.LENGTH_LONG).show()

                            // Limpa o histórico e volta para o ecrã de Login (MainActivity)
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@SettingsActivity, "Erro ao eliminar conta", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                        Toast.makeText(this@SettingsActivity, "Falha na ligação ao servidor", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            // Botão Negativo (Cancelar - não faz nada e fecha o diálogo)
            builder.setNegativeButton("Cancelar", null)

            // Mostra o diálogo no ecrã
            builder.show()
        }

    }

    private fun updateProfileImage(base64: String) {
        // Pega o ID do utilizador (podes passar via Intent ou usar SharedPreferences)
        // Por agora, vamos assumir que o ID vem da Intent que abriu as definições
        val userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do utilizador não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar o Retrofit exatamente como fizeste no Registo


        // Criamos um mapa com o campo que queremos mudar
        val updateData = mapOf("profileImage" to base64)

        // Nota: Precisas de ter a função 'updateUser' no teu ApiService.kt
        apiService.updateUser(userId, updateData).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SettingsActivity, "✅ Imagem atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "❌ Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@SettingsActivity, "🌐 Erro de ligação", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun mostrarDialogoMudarSenha(userId: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Nova Palavra-passe")

        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Atualizar") { _, _ ->
            val novaSenha = input.text.toString()
            if (novaSenha.isNotEmpty()) {
                // Aqui chamarias a rota da API que discutimos antes
                Toast.makeText(this, "A atualizar...", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}