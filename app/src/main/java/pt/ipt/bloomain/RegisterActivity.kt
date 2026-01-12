package pt.ipt.bloomain

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private var base64Image: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRegister: Button

    // Seletor de Imagem
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            findViewById<ImageView>(R.id.logo).setImageURI(it)
            val inputStream = contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        progressBar = findViewById(R.id.registerProgressBar)
        btnRegister = findViewById(R.id.registerButton)
        val profileCard = findViewById<View>(R.id.profileCard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Clicar no círculo da imagem abre a galeria
        profileCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val username = findViewById<EditText>(R.id.nameEditText).text.toString()
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val passwordConfirm = findViewById<EditText>(R.id.passwordConfirmationEditText).text.toString()

            // Validações
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "❌ As passwords não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Iniciar carregamento
            setLoading(true)

            val apiService = Retrofit.Builder()
                .baseUrl("http://192.168.1.211:3000/") // Garante que o IP está correto
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val request = RegisterRequest(username, email, password, base64Image)

            apiService.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "✅ Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                        finish() // Volta para o login
                    } else {
                        Toast.makeText(this@RegisterActivity, "❌ Erro: Utilizador ou email já existem", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "🌐 Falha na ligação ao servidor", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // Função para mostrar/esconder o carregamento
    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !isLoading
        btnRegister.alpha = if (isLoading) 0.5f else 1.0f
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}