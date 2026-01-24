package pt.ipt.bloomain

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private var base64Image: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRegister: Button

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

    /**
     * Gere o processo de criação de novas contas de utilizador.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        progressBar = findViewById(R.id.registerProgressBar)
        btnRegister = findViewById(R.id.registerButton)
        val profileCard = findViewById<View>(R.id.profileCard)

        val etUsername = findViewById<EditText>(R.id.nameEditText)
        val etEmail = findViewById<EditText>(R.id.emailEditText)
        val etPassword = findViewById<EditText>(R.id.passwordEditText)
        val etPasswordConfirm = findViewById<EditText>(R.id.passwordConfirmationEditText)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val passwordConfirm = etPasswordConfirm.text.toString()
            val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()

            if (username.length < 3) {
                etUsername.error = "O nome deve ter pelo menos 3 caracteres"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Insira um e-mail válido"
                return@setOnClickListener
            }

            if (!password.matches(passwordPattern)) {
                etPassword.error = "A password deve ter 8 carateres, incluir uma maiúscula, um número e um símbolo (@#$%^&+=!)"
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                etPasswordConfirm.error = "As passwords não coincidem"
                return@setOnClickListener
            }

            setLoading(true)

            val apiService = Retrofit.Builder()
                .baseUrl("http://192.168.1.211:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val request = RegisterRequest(username, email, password, base64Image)

            apiService.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val message = try {
                            JSONObject(errorBody).getString("message")
                        } catch (e: Exception) {
                            "Erro ao registar. Verifique os dados."
                        }
                        Toast.makeText(this@RegisterActivity, "$message", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "🌐 Falha na ligação ao servidor", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    /**
     * Controla a visibilidade dos indicadores de progresso e a interatividade dos botões.
     */
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