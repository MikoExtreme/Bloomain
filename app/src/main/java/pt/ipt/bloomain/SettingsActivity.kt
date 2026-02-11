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
import pt.ipt.bloomain.retrofitpackage.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

                // CORREÇÃO AQUI: Precisas de criar a instância da Data Class antes de a usar
                val dReq = DeleteRequest(loggedInUserId = userId)

                // Passamos 'dReq' que acabámos de criar
                RetrofitClient.instance.deleteAccount(userId, dReq).enqueue(object : Callback<PostResponse> {
                    override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SettingsActivity, "Conta eliminada.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SettingsActivity, "Não autorizado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                        Toast.makeText(this@SettingsActivity, "Erro de ligação ao servidor", Toast.LENGTH_SHORT).show()
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

        // CORREÇÃO AQUI: Precisas de criar a instância da Data Class ProfileImageRequest
        val pImgRequest = ProfileImageRequest(
            profileImage = base64,
            loggedInUserId = userId
        )

        // Passamos 'pImgRequest' que acabámos de criar
        RetrofitClient.instance.updateUser(userId, pImgRequest).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SettingsActivity, "Imagem atualizada!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@SettingsActivity, "Erro de rede", Toast.LENGTH_SHORT).show()
            }
        })
    }



}