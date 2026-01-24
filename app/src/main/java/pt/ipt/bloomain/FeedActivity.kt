package pt.ipt.bloomain

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.adapters.PostsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FeedActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userId: String

    // 1. Centralizamos o ApiService para o poderes usar em qualquer lado da classe
    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        userId = intent.getStringExtra("USER_ID") ?: ""

        // Botão de Criar Post
        val fabCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreatePostFeed)
        fabCreate.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // Configuração do RecyclerView
        recyclerView = findViewById(R.id.feedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. Lógica da Foto do Utilizador Logado (Circular no Topo)
        val ivUserLogged = findViewById<ImageView>(R.id.ivUserLogged)

        if (userId.isNotEmpty()) {
            apiService.getProfile(userId).enqueue(object : Callback<ProfileData> {
                override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                    if (response.isSuccessful) {
                        val profileImageBase64 = response.body()?.profileImage
                        if (!profileImageBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                ivUserLogged.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                Log.e("FEED_ERROR", "Erro ao decodificar imagem: ${e.message}")
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                    Log.e("FEED_ERROR", "Erro ao buscar perfil: ${t.message}")
                }
            })
        }

        // 3. Clique na foto do topo para abrir o próprio perfil
        findViewById<View>(R.id.cvUserLogged).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("CURRENT_USER_ID", userId)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnOpenSearch).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("CURRENT_USER_ID", userId)
            startActivity(intent)
        }

        loadFeed()
    }

    private fun loadFeed() {
        apiService.getFeed().enqueue(object : Callback<List<PostItemResponse>> {
            override fun onResponse(call: Call<List<PostItemResponse>>, response: Response<List<PostItemResponse>>) {
                if (response.isSuccessful) {
                    val allPosts = response.body() ?: emptyList()

                    // ATUALIZAÇÃO AQUI: Passamos apiService e onDelete
                    recyclerView.adapter = PostsAdapter(
                        items = allPosts,
                        currentUserId = userId,
                        apiService = apiService, // Passa o apiService global da Activity
                        onLike = { post -> likePost(post._id) },
                        onDelete = { postId -> loadFeed() } // Recarrega o feed após apagar
                    )
                } else {
                    Toast.makeText(this@FeedActivity, "Erro no Servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<PostItemResponse>>, t: Throwable) {
                Toast.makeText(this@FeedActivity, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun likePost(postId: String) {
        val body = mapOf("userId" to userId)
        apiService.toggleLike(postId, body).enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    loadFeed()
                } else {
                    Toast.makeText(this@FeedActivity, "Erro ao dar like", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                Log.e("FEED_DEBUG", "Falha na rede: ${t.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadFeed()
    }
}