package pt.ipt.bloomain.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.R
import pt.ipt.bloomain.feed.PostDetailActivity
import pt.ipt.bloomain.retrofit_api.PostItemResponse


/**
* Adaptador das publicações dentro do perfil do utilizador que os criou em formato Grid
* items -> Lista de publicações [PostItemResponse] em exibição
* currentUserId -> ID do utilizador autenticado
* onClick -> Callback para capturar o evento de clique na publicação selecionada
* */

class ProfilePostsAdapter(
    private val items: List<PostItemResponse>,
    private val currentUserId: String, // 1. ADICIONA ESTA LINHA AQUI
    private val onClick: (PostItemResponse) -> Unit
) : RecyclerView.Adapter<ProfilePostsAdapter.GridViewHolder>() {

    /**
    * ViewHolder para a grid, mostra a imagem da publicação
    * */
    inner class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPostImage: ImageView = view.findViewById(R.id.iv_grid_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post_grid, parent, false)
        return GridViewHolder(v)
    }
    /**
     * Vincula os dados da publicação à célula da grid e configura o evento de clique.
     * holder -> O ViewHolder já preenchido
     * position -> A posição da publicação na lista de dados
     */
    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val post = items[position]

        // Processamento da imagem
        if (post.postImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.postImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivPostImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Configuração do evento de navegação
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // Ao clicar na imagem da publicação, vai para os detalhes da publicação
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("POST_ID", post._id)
                putExtra("CURRENT_USER_ID", currentUserId) // Agora o currentUserId já existe!
            }
            context.startActivity(intent)

            onClick(post)
        }
    }

    /**
     *  Retorna o tamanho da lista de publicações do utilizador
     */
    override fun getItemCount(): Int = items.size
}