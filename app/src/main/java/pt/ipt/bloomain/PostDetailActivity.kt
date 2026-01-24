package pt.ipt.bloomain

import android.os.Bundle
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

class PostDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var currentUserId: String

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val postId = intent.getStringExtra("POST_ID") ?: ""
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""

        recyclerView = findViewById(R.id.detailRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadSinglePost(postId)
    }

    private fun loadSinglePost(postId: String) {
        apiService.getPostById(postId).enqueue(object : Callback<PostItemResponse> {
            override fun onResponse(call: Call<PostItemResponse>, response: Response<PostItemResponse>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    if (post != null) {
                        // REUTILIZAMOS o PostsAdapter do Feed
                        recyclerView.adapter = PostsAdapter(
                            items = listOf(post),
                            currentUserId = currentUserId,
                            apiService = apiService,
                            onLike = { clickedPost -> toggleLike(clickedPost._id) },
                            onDelete = { finish() } // Se apagar, fecha o ecrã e volta ao perfil
                        )
                    }
                }
            }
            override fun onFailure(call: Call<PostItemResponse>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "Erro ao carregar post", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleLike(postId: String) {
        val body = mapOf("userId" to currentUserId)
        apiService.toggleLike(postId, body).enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    loadSinglePost(postId) // Atualiza a estrela
                }
            }
            override fun onFailure(call: Call<PostResponse>, t: Throwable) {}
        })
    }
}