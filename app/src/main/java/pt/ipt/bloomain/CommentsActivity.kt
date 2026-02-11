package pt.ipt.bloomain

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.adapters.CommentsAdapter
import pt.ipt.bloomain.retrofitpackage.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnSend: Button
    private lateinit var balls: Button

    private lateinit var postId: String
    private lateinit var userId: String

    /**
     * Inicializa a Activity de comentários.
     * Este método é responsável por recuperar os dados da Intent anterior,
     * configurar a interface visual (RecyclerView) e iniciar a carga
     * de dados vinda do servidor Node.js.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)


        postId = intent.getStringExtra("POST_ID") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""


        recyclerView = findViewById(R.id.rvComments)
        etComment = findViewById(R.id.etComment)
        btnSend = findViewById(R.id.btnSendComment)

        recyclerView.layoutManager = LinearLayoutManager(this)


        loadComments()


        btnSend.setOnClickListener {
            val text = etComment.text.toString()
            if (text.isNotEmpty()) {
                sendComment(text)
            }
        }
    }

    /**
     * Faz uma requisição assíncrona ao servidor para obter a lista de comentários.
     * * Este método utiliza o Retrofit para contactar o endpoint GET /posts/:postId/comments.
     * Em caso de sucesso, atualiza o RecyclerView com a lista recebida.
     * Em caso de erro de rede ou resposta inválida, exibe um Toast ao utilizador.
     */
    private fun loadComments() {
        RetrofitClient.instance.getComments(postId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val commentsList = response.body() ?: emptyList()
                    recyclerView.adapter = CommentsAdapter(commentsList)
                } else {
                    // Adicionamos validação de erro conforme o ponto 57 do enunciado
                    Toast.makeText(this@CommentsActivity, "Erro do servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Toast.makeText(this@CommentsActivity, "Falha de rede: Verifique a ligação", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Envia um novo comentário para o servidor e atualiza a interface.
     * * Este método encapsula a descrição, o ID do autor e o ID do post num objeto [CommentRequest].
     * Faz uma chamada POST para o endpoint /comments. Em caso de sucesso:
     * 1. Limpa o campo de texto.
     * 2. Esconde o teclado.
     * 3. Chama [loadComments] para sincronizar a lista com o novo conteúdo da base de dados.
     * @param description O texto do comentário inserido pelo utilizador.
     */
    private fun sendComment(description: String) {
        val request = CommentRequest(creatorId = userId, postId = postId, description = description)

        RetrofitClient.instance.addComment(request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    etComment.text.clear()
                    hideKeyboard()
                    loadComments()
                    Toast.makeText(this@CommentsActivity, "Comentado!", Toast.LENGTH_SHORT).show()
                } else {
                    // Mensagens de erro adequadas são obrigatórias
                    Toast.makeText(this@CommentsActivity, "Erro ao comentar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@CommentsActivity, "Sem ligação ao servidor local", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * Fecha o teclado virtual de forma programática.
     * * Este método identifica qual o componente que detém o foco atual na Activity
     * e utiliza o InputMethodManager para ocultar o teclado. É ideal para ser
     * chamado após o envio de um comentário ou ao navegar para outra tela.
     */
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}