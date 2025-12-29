package pt.ipt.bloomain

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        // Configuração do RecyclerView para Grelha de 3 colunas
        val recyclerView = findViewById<RecyclerView>(R.id.postsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Ajuste de preenchimento para barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = intent.getStringExtra("USER_ID") ?: ""

        // --- LÓGICA DO RETROFIT ---
        val BASE_URL = "http://192.168.1.211:3000/"

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getProfile(userId).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    val profile = response.body()

                    // Atualizar Textos
                    findViewById<TextView>(R.id.usernameTextView).text = profile?.username
                    findViewById<TextView>(R.id.emailTextView).text = profile?.bio

                    // --- DECODIFICAR E MOSTRAR A IMAGEM DE PERFIL ---
                    val base64String = profile?.profileImage
                    if (!base64String.isNullOrEmpty()) {
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        findViewById<android.widget.ImageView>(R.id.profileImage).setImageBitmap(decodedImage)
                    }

                    // Atualizar Estatísticas
                    findViewById<TextView>(R.id.postsCountText).text = profile?.stats?.posts.toString()
                    findViewById<TextView>(R.id.followerCountText).text = profile?.stats?.followers.toString()
                    findViewById<TextView>(R.id.followingCountText).text = profile?.stats?.following.toString()

                    val numPosts = profile?.stats?.posts ?: 0
                    recyclerView.adapter = PostAdapter(numPosts)

                } else {
                    Toast.makeText(this@ProfileActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Falha na ligação: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Lógica para fechar teclado
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