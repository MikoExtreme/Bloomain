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

    /**
     * Gere as configurações da conta e preferências do utilizador.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val currentUserName = intent.getStringExtra("CURRENT_USERNAME") ?: ""

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditCredentialsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("CURRENT_USERNAME", currentUserName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.item_section_image).setOnClickListener {
            val intent = Intent(this, EditImageActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }


        findViewById<ImageButton>(R.id.go_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.log_out).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        btnDeleteAccount.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Atenção!")
            builder.setMessage("Tens a certeza que queres eliminar a tua conta? Todos os teus posts e comentários serão apagados permanentemente.")

            builder.setPositiveButton("Sim, Eliminar") { _, _ ->
                val userId = intent.getStringExtra("USER_ID") ?: ""

                apiService.deleteAccount(userId).enqueue(object : Callback<PostResponse> {
                    override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SettingsActivity, "Conta eliminada com sucesso", Toast.LENGTH_LONG).show()

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

            builder.setNegativeButton("Cancelar", null)

            builder.show()
        }

    }

    /**
     * Envia uma nova representação Base64 da imagem de perfil diretamente para o servidor.
     */
    private fun updateProfileImage(base64: String) {
        val userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do utilizador não encontrado", Toast.LENGTH_SHORT).show()
            return
        }


        val updateData = mapOf("profileImage" to base64)

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