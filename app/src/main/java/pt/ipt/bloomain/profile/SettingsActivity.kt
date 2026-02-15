package pt.ipt.bloomain.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.bloomain.R
import pt.ipt.bloomain.authentication.MainActivity
import pt.ipt.bloomain.retrofit_api.DeleteRequest
import pt.ipt.bloomain.retrofit_api.PostResponse
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity responsável pelas definições da conta do utilizador autenticado,
 * onde aparece um menu com várias opções de gestão da conta
 */
class SettingsActivity : AppCompatActivity() {

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


        // Lógica do Logout
        // Limpa os dados da sessão guardados localmente e redireciona para a página de Login
        findViewById<Button>(R.id.log_out).setOnClickListener {
            val sharedPrefs = getSharedPreferences("BloomainPrefs", MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }



        // Lógica de eliminação da conta
        // Mostra uma mensagem de confirmação antes da eliminação completa da conta e de tudo relacionada a ela
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        btnDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Atenção!")
            builder.setMessage("Tens a certeza que queres eliminar a tua conta? Todos os teus posts e comentários serão apagados permanentemente.")

            builder.setPositiveButton("Sim, Eliminar") { _, _ ->
                val userId = intent.getStringExtra("USER_ID") ?: ""
                val dReq = DeleteRequest(loggedInUserId = userId)

                RetrofitClient.instance.deleteAccount(userId, dReq).enqueue(object :
                    Callback<PostResponse> {
                    override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                        if (response.isSuccessful) {
                            val sharedPrefs = getSharedPreferences("BloomainPrefs", MODE_PRIVATE)
                            sharedPrefs.edit().clear().apply()

                            Toast.makeText(this@SettingsActivity, "Conta eliminada com sucesso.", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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

}