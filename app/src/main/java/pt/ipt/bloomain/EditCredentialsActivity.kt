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

    /**
     *Inicializa a Activity de edição de credenciais e perfil.
     * * Este método realiza as seguintes operações:
     *  1. Recupera o ID do utilizador e o nome atual vindos da Intent para preencher os campos.
     *  2. Posiciona o cursor de edição no final do texto para facilitar a interação.
     *  3. Configura o botão de salvaguarda com validações em tempo real:
     *  - Impede o envio se todos os campos estiverem vazios.
     *  - Valida a coincidência entre a nova palavra-passe e a confirmação.
     *  - Verifica se a nova palavra-passe cumpre o requisito mínimo de 6 caracteres.
     *  4. Dispara a atualização no servidor via [updateUser] apenas se os dados forem válidos.
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
     * * Este método realiza as seguintes ações:
     * 1. Inicializa a instância do Retrofit configurada para o servidor local.
     * 2. Constrói um mapa dinâmico contendo apenas os campos preenchidos
     * (username, password e/ou bio), evitando o envio de dados vazios.
     * 3. Executa uma chamada à API através do método 'updateUser'.
     * 4. Em caso de sucesso, exibe uma confirmação e encerra a Activity com [finish].
     * 5. Em caso de falha ou erro de resposta, notifica o utilizador via Toast.
     */
    private fun updateUser(id: String, username: String, password: String, bio: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val updateData = mutableMapOf<String, String>()

        updateData["loggedInUserId"] = id

        if (username.isNotEmpty()) updateData["username"] = username
        if (password.isNotEmpty()) updateData["password"] = password
        if (bio.isNotEmpty()) updateData["bio"] = bio

        apiService.updateUser(id, updateData).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditCredentialsActivity, "Alterado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditCredentialsActivity, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@EditCredentialsActivity, "Falha na ligação", Toast.LENGTH_SHORT).show()
            }
        })
    }
}