package pt.ipt.bloomain.authentication

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.ipt.bloomain.R
import pt.ipt.bloomain.authentication.RegisterActivity
import pt.ipt.bloomain.feed.FeedActivity
import pt.ipt.bloomain.retrofit_api.LoginRequest
import pt.ipt.bloomain.retrofit_api.LoginResponse
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
* Activity responsável pela autenticação dos utilizadores
* */
class MainActivity : AppCompatActivity() {

    // Nome do ficheiro de preferências, para que o utilizador não tenha de fazer autenticação sempre que entre na aplicação
    private val PREFS_NAME = "BloomainPrefs"


    /**
     * Inicializa a Activity principal de Login.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lógica de persistência
        // Verificamos se o SharedPreferences tem o USER_ID guardado anteriormente
        // Se existir, vai para o layout correspondente ao feed
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUserId = sharedPrefs.getString("USER_ID", null)

        if(savedUserId != null){
            goToFeed(savedUserId)
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.login)



        val btnAbout = findViewById<Button>(R.id.btnAboutLogin)

        btnAbout.setOnClickListener {
            // Abrir a página de autores e bibliotecas
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        // Lógica de autenticação
        // Valida o email e a palavra-passe, com o que existe na base de dados
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preenche todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }






            val loginRequest = LoginRequest(email, password)


            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val userId = response.body()?.userId

                        // Guarda a sessão do utilizador autenticado, para futuro uso da aplicação
                        val sharedPrefs = getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                        with(sharedPrefs.edit()){
                            putString("USER_ID", userId)
                            apply()
                        }

                        goToFeed(userId)

                    } else {
                        Toast.makeText(this@MainActivity, "Credenciais inválidas. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Erro de ligação: Servidor local inacessível", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    /**
     * Gere a propagação de eventos de toque para fechar o teclado automaticamente.
     */
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

    /**
     * Direciona para o feed
     */
    private fun goToFeed(id: String?) {
        val intent = Intent(this, FeedActivity::class.java)
        intent.putExtra("USER_ID", id)
        startActivity(intent)
        finish()
    }
}