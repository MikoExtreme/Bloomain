package pt.ipt.bloomain

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.adapters.CommentsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnSend: Button

    private lateinit var postId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // 1. Recuperar IDs da Intent
        postId = intent.getStringExtra("POST_ID") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""

        // 2. Inicializar UI
        recyclerView = findViewById(R.id.rvComments)
        etComment = findViewById(R.id.etComment)
        btnSend = findViewById(R.id.btnSendComment)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Carregar comentários existentes
        loadComments()

        // 4. Configurar envio de comentário
        btnSend.setOnClickListener {
            val text = etComment.text.toString()
            if (text.isNotEmpty()) {
                sendComment(text)
            }
        }
    }

    private fun loadComments() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        apiService.getComments(postId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val commentsList = response.body() ?: emptyList()
                    recyclerView.adapter = CommentsAdapter(commentsList)
                }
            }
            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Toast.makeText(this@CommentsActivity, "Erro ao carregar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendComment(description: String) {
        val apiService = Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val request = CommentRequest(creatorId = userId, postId = postId, description = description)

        apiService.addComment(request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    etComment.text.clear()
                    hideKeyboard()
                    loadComments() // Recarrega a lista para mostrar o novo comentário
                    Toast.makeText(this@CommentsActivity, "Comentado!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@CommentsActivity, "Erro ao enviar", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Função utilitária para esconder o teclado
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}