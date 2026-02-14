package pt.ipt.bloomain.adapters

import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.R
import pt.ipt.bloomain.retrofit_api.Comment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/**
* Adaptador responsável por gerir e exibir a lista de comentários de uma publicação num RecyclerView
* comments -> Lista de objetos [Comment] (comentários) em exibição
* currentUserId -> ID do utilizador atual/autenticado
* onDeleteClick -> Função de callback acionada ao clicar no botão de eliminar comentário
* */
class CommentsAdapter(private val comments: List<Comment>,
                      private val currentUserId: String,
                      private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    /**
    * ViewHolder que armazena as referências dos componentes visuais de cada parte que constitui um comentário
    * */

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivCommentAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvCommentUsername)
        val tvDescription: TextView = view.findViewById(R.id.tvCommentDescription)
        val tvTime: TextView? = view.findViewById(R.id.tvCommentTime)

        val btnDelete: ImageView? = view.findViewById(R.id.btnDeleteComment)}

    /**
     * Cria uma nova instância de [CommentViewHolder] com o layout específico
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(v)
    }

    /**
     * Vincula os dados de um comentário específico aos componentes visuais do ViewHolder.
     * Trata da lógica de tempo relativo, conversão de imagem e segurança, respetivamente á eliminação de comentários
     */
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]


        holder.tvUsername.text = comment.creator.username
        holder.tvDescription.text = comment.description

        try {

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val time = sdf.parse(comment.createdAt)?.time ?: 0
            val now = System.currentTimeMillis()

            // DateUtils converte o timestamp em texto como "há 5 minutos"
            val relativeTime = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            holder.tvTime?.text = relativeTime
        } catch (e: Exception) {
            holder.tvTime?.text = "agora"
        }

        // Lógica de segurança
        // Apenas o criador do comentário pode apagá-lo
        if (comment.creator._id == currentUserId) {
            holder.btnDelete?.visibility = View.VISIBLE
            holder.btnDelete?.setOnClickListener {
                onDeleteClick(comment._id)
            }
        } else {
            holder.btnDelete?.visibility = View.GONE
        }


        if (comment.creator.profileImage.isNotEmpty()) {
            val imageBytes = Base64.decode(comment.creator.profileImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.ivAvatar.setImageBitmap(bitmap)
        }
    }

    /*
    * Retorna o tamanho da lista de comentários*/

    override fun getItemCount(): Int = comments.size
}