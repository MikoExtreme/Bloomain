package pt.ipt.bloomain

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
import pt.ipt.bloomain.retrofitpackage.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    /**
     * Inicializa a Activity principal de Login.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preenche todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }






            val loginRequest = LoginRequest(username, password)


            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val userId = response.body()?.userId

                        val intent = Intent(this@MainActivity, FeedActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish() // Impede o utilizador de voltar ao login com o botão "Back"
                    } else {
                        // Requisito 57: Mensagem de erro adequada
                        Toast.makeText(this@MainActivity, "Credenciais inválidas. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // Requisito 57: Notificar se o servidor local não estiver ligado
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
}