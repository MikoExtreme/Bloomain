package pt.ipt.bloomain.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.bloomain.R
import pt.ipt.bloomain.retrofit_api.ProfileData
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import pt.ipt.bloomain.retrofit_api.UpdateUserRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity responsável pela gestão e edição das informações de um utilizador (nome, palavra-passe e foto de perfil)
 */
class EditCredentialsActivity : AppCompatActivity() {

    /**
     * Inicializa a Activity de edição de credenciais e perfil.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_credentials)

        val userId = intent.getStringExtra("USER_ID") ?: ""

        val editNameField = findViewById<EditText>(R.id.editNewUsername)


        val currentName = intent.getStringExtra("CURRENT_USERNAME")

        if (!currentName.isNullOrEmpty()) {
            editNameField.setText(currentName)
            editNameField.setSelection(editNameField.text.length)
        }

        // Validações das credenciais
        findViewById<Button>(R.id.btnSaveCredentials).setOnClickListener {
            val newUsername = findViewById<EditText>(R.id.editNewUsername).text.toString().trim()
            val newPassword = findViewById<EditText>(R.id.editNewPassword).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.editConfirmPassword).text.toString().trim()
            val newBio = findViewById<EditText>(R.id.editBio).text.toString().trim()


            if (newUsername.isEmpty() && newPassword.isEmpty() && newBio.isEmpty()) {
                Toast.makeText(this, "Preencha pelo menos um campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (newPassword.isNotEmpty()) {
                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "As palavras-passe não coincidem!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "A palavra-passe deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            updateUser(userId, newUsername, newPassword, newBio)
        }
    }
    /**
     * Envia as atualizações de perfil do utilizador para o servidor de forma assíncrona.
     */
    private fun updateUser(id: String, username: String, password: String, bio: String) {

        val request = UpdateUserRequest(
            loggedInUserId = id,
            username = if (username.isNotEmpty()) username else null,
            password = if (password.isNotEmpty()) password else null,
            bio = if (bio.isNotEmpty()) bio else null
        )

        RetrofitClient.instance.updateUser(id, request).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditCredentialsActivity, "Sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditCredentialsActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@EditCredentialsActivity, "Falha de rede", Toast.LENGTH_SHORT).show()
            }
        })
    }
}