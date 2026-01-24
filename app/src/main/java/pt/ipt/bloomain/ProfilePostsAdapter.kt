package pt.ipt.bloomain.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.PostDetailActivity
import pt.ipt.bloomain.R
import pt.ipt.bloomain.PostItemResponse

class ProfilePostsAdapter(
    private val items: List<PostItemResponse>,
    private val currentUserId: String, // 1. ADICIONA ESTA LINHA AQUI
    private val onClick: (PostItemResponse) -> Unit
) : RecyclerView.Adapter<ProfilePostsAdapter.GridViewHolder>() {

    inner class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPostImage: ImageView = view.findViewById(R.id.iv_grid_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post_grid, parent, false)
        return GridViewHolder(v)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val post = items[position]

        if (post.postImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.postImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivPostImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Configura o clique para abrir o detalhe
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("POST_ID", post._id)
                putExtra("CURRENT_USER_ID", currentUserId) // Agora o currentUserId já existe!
            }
            context.startActivity(intent)

            // Também executa o callback original se necessário
            onClick(post)
        }
    }

    override fun getItemCount(): Int = items.size
}