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
import pt.ipt.bloomain.Comment
import pt.ipt.bloomain.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CommentsAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivCommentAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvCommentUsername)
        val tvDescription: TextView = view.findViewById(R.id.tvCommentDescription)
        val tvTime: TextView? = view.findViewById(R.id.tvCommentTime)    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(v)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // 1. Nome e Descrição
        holder.tvUsername.text = comment.creator.username
        holder.tvDescription.text = comment.description

        try {
            // Formato que vem do servidor (ISO 8601)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val time = sdf.parse(comment.createdAt)?.time ?: 0
            val now = System.currentTimeMillis()

            // Cria a frase "há X minutos" automaticamente
            val relativeTime = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            holder.tvTime?.text = relativeTime
        } catch (e: Exception) {
            holder.tvTime?.text = "agora"
        }

        // 2. Foto de Perfil do utilizador que comentou (Base64)
        if (comment.creator.profileImage.isNotEmpty()) {
            val imageBytes = Base64.decode(comment.creator.profileImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.ivAvatar.setImageBitmap(bitmap)
        }
    }

    override fun getItemCount(): Int = comments.size
}