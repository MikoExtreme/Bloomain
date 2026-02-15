package pt.ipt.bloomain.feed

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.R
import pt.ipt.bloomain.adapters.PostsAdapter
import pt.ipt.bloomain.retrofit_api.LikeRequest
import pt.ipt.bloomain.retrofit_api.PostItemResponse
import pt.ipt.bloomain.retrofit_api.PostResponse
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity responsável por exibir os detalhes de uma publicação
 * É invocada normalmente ao clicar numa publicação presente na grid do perfil
 */
class PostDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var currentUserId: String



    /**
     * Inicializa a Activity de detalhes de uma publicação específica.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val postId = intent.getStringExtra("POST_ID") ?: ""
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""

        recyclerView = findViewById(R.id.detailRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadSinglePost(postId)
    }

    /**
     * Procura os detalhes de uma publicação específica no servidor e atualiza a interface.
     */
    private fun loadSinglePost(postId: String) {
        RetrofitClient.instance.getPostById(postId).enqueue(object : Callback<PostItemResponse> {
            override fun onResponse(call: Call<PostItemResponse>, response: Response<PostItemResponse>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    if (post != null) {
                        recyclerView.adapter = PostsAdapter(
                            items = listOf(post), // O adaptador recebe uma lista com apenas um item
                            currentUserId = currentUserId,
                            apiService = RetrofitClient.instance,
                            onLike = { clickedPost -> toggleLike(clickedPost._id) },
                            onDelete = {
                                // Se o utilizador apagar a publicação nesta tela, voltamos para o perfil
                                Toast.makeText(
                                    this@PostDetailActivity,
                                    "Post removido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        )
                    }
                } else {
                    Toast.makeText(this@PostDetailActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostItemResponse>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "Sem ligação ao servidor local", Toast.LENGTH_SHORT).show()
            }
        })
    }
/**
 * Alterna o estado de 'Like' de uma publicação para o utilizador atual.
 */
private fun toggleLike(postId: String) {
    val request = LikeRequest(userId = currentUserId)

    RetrofitClient.instance.toggleLike(postId, request).enqueue(object : Callback<PostResponse> {
        override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
            if (response.isSuccessful) {
                loadSinglePost(postId)
            } else {
                Toast.makeText(this@PostDetailActivity, "Não foi possível processar o Like", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<PostResponse>, t: Throwable) {
            Toast.makeText(this@PostDetailActivity, "Erro de rede ao dar Like", Toast.LENGTH_SHORT).show()
        }
    })
}
}