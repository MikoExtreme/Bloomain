package pt.ipt.bloomain

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.ProfileActivity
import pt.ipt.bloomain.ProfileData
import pt.ipt.bloomain.R

class UserSearchAdapter(
    private val users: List<ProfileData>,
    private val currentUserId: String
) : RecyclerView.Adapter<UserSearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivSearchAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvSearchUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user_search, parent, false)
        return SearchViewHolder(v)
    }

    /**
     * Associa os dados do perfil pesquisado aos componentes visuais da linha.
     */
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username

        if (!user.profileImage.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(user.profileImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivAvatar.setImageResource(R.drawable.logo_oficial_bloomainv2)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java).apply {
                putExtra("USER_ID", user._id) // ID do utilizador encontrado
                putExtra("CURRENT_USER_ID", currentUserId) // O teu ID
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = users.size
}