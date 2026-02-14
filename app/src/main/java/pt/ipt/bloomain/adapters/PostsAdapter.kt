package pt.ipt.bloomain.adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.profile.ProfileActivity
import pt.ipt.bloomain.R
import pt.ipt.bloomain.feed.ViewLocationActivity
import pt.ipt.bloomain.feed.CommentsActivity
import pt.ipt.bloomain.retrofit_api.ApiService
import pt.ipt.bloomain.retrofit_api.DeleteRequest
import pt.ipt.bloomain.retrofit_api.PostItemResponse
import pt.ipt.bloomain.retrofit_api.PostResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/**
* Adaptador do feed, onde são processados as publicações
* Gera a exibição das publicações, imagens, gostos (Likes), comentários, localização e eliminação das publicações
*
* items -> Lista de publicações [PostItemResponse]
* currentUserId -> ID do utilizador atual/autenticado
* apiService -> Instância da interface Retrofit
* onLike -> Callback para processar a lógica dos gostos (dar "Like" ou remover "Like") no FeedActivity
* onDelete -> Callback para notificar o FeedActivity que uma publicação foi removida
* */

class PostsAdapter(
    private val items: List<PostItemResponse>,
    private val currentUserId: String,
    private val apiService: ApiService,
    private val onLike: (PostItemResponse) -> Unit = {},
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    /**
    * ViewHolder que mapeia os componentes do layout
    * */

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val ivPostImage: ImageView = view.findViewById(R.id.iv_post_image)
        val btnLike: ImageButton = view.findViewById(R.id.btn_like)
        val btnComment: ImageButton = view.findViewById(R.id.btn_comment)
        val btnDeletePost: ImageButton = view.findViewById(R.id.btn_delete_post) // IMPORTANTE: Declarar aqui
        val tvLikes: TextView = view.findViewById(R.id.tv_likes)
        val tvCaption: TextView = view.findViewById(R.id.tv_caption)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvViewComments: TextView = view.findViewById(R.id.tv_view_comments)

        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
    }

    /**
     * Cria a estrutura física de cada item da lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(v)
    }

    /**
     * Alimenta a interface com os dados dinâmicos do post e configura as interações.
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = items[position]


        // Lógica de localização
        val coords = post.location // Campo que adicionaste à Data Class


        // Se existirem coordenadas, ativamos a navegação para o Google Maps
        if (!coords.isNullOrEmpty() && coords.contains(",")) {
            holder.tvLocation.visibility = View.VISIBLE
            holder.tvLocation.text =  "Ver no Mapa"
            holder.tvLocation.setOnClickListener {
                val intent = Intent(holder.itemView.context, ViewLocationActivity::class.java)
                intent.putExtra("COORDINATES", coords)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            // Caso não haja local, mostra o texto mas sem clique
            holder.tvLocation.visibility = View.VISIBLE
            holder.tvLocation.text = "Local não especificado"
            holder.tvLocation.setOnClickListener(null)
        }


        holder.tvUsername.text = post.creator.username
        holder.tvCaption.text = "${post.creator.username} ${post.description}"
        holder.tvLikes.text = "${post.likes.size} gostos"

        val openProfile = View.OnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java).apply {
                putExtra("USER_ID", post.creator._id)
                putExtra("CURRENT_USER_ID", currentUserId)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.ivAvatar.setOnClickListener(openProfile)
        holder.tvUsername.setOnClickListener(openProfile)

        // Formatação da data
        try {
            val localePT = Locale.forLanguageTag("pt-PT")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", localePT)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val time = sdf.parse(post.createdAt)?.time ?: 0
            val now = System.currentTimeMillis()

            val relativeTime = DateUtils.getRelativeTimeSpanString(
                time,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()

            holder.tvTime.text = relativeTime
        } catch (e: Exception) {
            holder.tvTime.text = "há algum tempo"
        }

        // Processamento de imagens
        if (post.creator.profileImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.creator.profileImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("ADAPTER_DEBUG", "Erro avatar")
            }
        }


        if (post.postImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.postImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivPostImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("ADAPTER_DEBUG", "Erro imagem post")
            }
        }

        val isLikedByMe = post.likes.contains(currentUserId)
        if (isLikedByMe) {
            holder.btnLike.setImageResource(android.R.drawable.btn_star_big_on)
            holder.btnLike.setColorFilter(Color.parseColor("#FFD700"))
        } else {
            holder.btnLike.setImageResource(android.R.drawable.btn_star_big_off)
            holder.btnLike.setColorFilter(Color.BLACK)
        }
        holder.btnLike.setOnClickListener { onLike(post) }


        // Navegação para comentários
        val abrirComentarios = View.OnClickListener {
            val intent = Intent(holder.itemView.context, CommentsActivity::class.java).apply {
                putExtra("POST_ID", post._id)
                putExtra("USER_ID", currentUserId)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.btnComment.setOnClickListener(abrirComentarios)
        holder.tvViewComments.setOnClickListener(abrirComentarios)


        // Lógica de eliminação de publicações
        if (post.creator._id == currentUserId) {
            // Apenas o dono pode eliminar a sua publicação
            holder.btnDeletePost.visibility = View.VISIBLE
            holder.btnDeletePost.setOnClickListener {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Apagar Publicação")
                builder.setMessage("Tens a certeza que queres eliminar este post?")

                builder.setPositiveButton("Eliminar") { _, _ ->

                    val deleteRequest = DeleteRequest(loggedInUserId = currentUserId)

                    apiService.deletePost(post._id, deleteRequest).enqueue(object :
                        Callback<PostResponse> {
                        override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(holder.itemView.context, "Post removido com sucesso", Toast.LENGTH_SHORT).show()
                                onDelete(post._id)
                            } else {
                                // Requisito 57: Mensagem de erro adequada
                                val msg = if (response.code() == 403) "Não autorizado!" else "Erro ao apagar"
                                Toast.makeText(holder.itemView.context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                            Toast.makeText(holder.itemView.context, "Erro de rede: Servidor Offline", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                builder.setNegativeButton("Cancelar", null)
                builder.show()
            }
        } else {
            holder.btnDeletePost.visibility = View.GONE
        }
    }

    /**
     * Retorna o tamanho da lista de publicações
     */
    override fun getItemCount(): Int = items.size
}