package pt.ipt.bloomain.adapters

import android.content.Intent
import android.graphics.BitmapFactory
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
import pt.ipt.bloomain.ApiService
import pt.ipt.bloomain.CommentsActivity
import pt.ipt.bloomain.R
import pt.ipt.bloomain.PostItemResponse
import pt.ipt.bloomain.PostResponse
import pt.ipt.bloomain.ProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostsAdapter(
    private val items: List<PostItemResponse>,
    private val currentUserId: String,
    private val apiService: ApiService,
    private val onLike: (PostItemResponse) -> Unit = {},
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    // 1. ViewHolder com todos os elementos mapeados
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(v)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = items[position]

        // --- DADOS DE TEXTO ---
        holder.tvUsername.text = post.creator.username
        holder.tvCaption.text = "${post.creator.username} ${post.description}"
        holder.tvLikes.text = "${post.likes.size} gostos"

        // --- NAVEGAÇÃO PARA PERFIL ---
        val abrirPerfil = View.OnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java).apply {
                putExtra("USER_ID", post.creator._id)
                putExtra("CURRENT_USER_ID", currentUserId)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.ivAvatar.setOnClickListener(abrirPerfil)
        holder.tvUsername.setOnClickListener(abrirPerfil)

        // --- DATA E HORA (PORTUGUÊS) ---
        try {
            val localePT = java.util.Locale.forLanguageTag("pt-PT")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", localePT)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val time = sdf.parse(post.createdAt)?.time ?: 0
            val now = System.currentTimeMillis()

            val relativeTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                time,
                now,
                android.text.format.DateUtils.MINUTE_IN_MILLIS,
                android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()

            holder.tvTime.text = relativeTime
        } catch (e: Exception) {
            holder.tvTime.text = "há algum tempo"
        }

        // --- IMAGENS (AVATAR E POST) ---
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

        // --- LÓGICA DE LIKE ---
        val isLikedByMe = post.likes.contains(currentUserId)
        if (isLikedByMe) {
            holder.btnLike.setImageResource(android.R.drawable.btn_star_big_on)
            holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
        } else {
            holder.btnLike.setImageResource(android.R.drawable.btn_star_big_off)
            holder.btnLike.setColorFilter(android.graphics.Color.BLACK)
        }
        holder.btnLike.setOnClickListener { onLike(post) }

        // --- LÓGICA DE COMENTÁRIOS ---
        val abrirComentarios = View.OnClickListener {
            val intent = Intent(holder.itemView.context, CommentsActivity::class.java).apply {
                putExtra("POST_ID", post._id)
                putExtra("USER_ID", currentUserId)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.btnComment.setOnClickListener(abrirComentarios)
        holder.tvViewComments.setOnClickListener(abrirComentarios)

        // --- LÓGICA DE ELIMINAR POST ---
        if (post.creator._id == currentUserId) {
            holder.btnDeletePost.visibility = View.VISIBLE
            holder.btnDeletePost.setOnClickListener {
                val builder = android.app.AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Apagar Publicação")
                builder.setMessage("Tens a certeza que queres eliminar este post?")

                builder.setPositiveButton("Eliminar") { _, _ ->
                    // Criar o mapa de segurança para o servidor
                    val securityBody = mapOf("loggedInUserId" to currentUserId)

                    // Agora passamos os DOIS parâmetros: post._id e o securityBody
                    apiService.deletePost(post._id, securityBody).enqueue(object : Callback<PostResponse> {
                        override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(holder.itemView.context, "Post removido", Toast.LENGTH_SHORT).show()
                                onDelete(post._id)
                            } else {
                                Toast.makeText(holder.itemView.context, "Não autorizado", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                            Toast.makeText(holder.itemView.context, "Erro de rede", Toast.LENGTH_SHORT).show()
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

    override fun getItemCount(): Int = items.size
}